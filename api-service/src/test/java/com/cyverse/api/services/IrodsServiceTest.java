package com.cyverse.api.services;

import com.cyverse.api.config.IrodsServiceConfig;
import com.cyverse.api.exceptions.IrodsException;
import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class IrodsServiceTest {
    @Mock
    private IrodsServiceConfig config;

    @Spy
    @InjectMocks
    IrodsService irodsService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddUserToIrods() throws IrodsException, IOException,
            InterruptedException, ResourceAlreadyExistsException {
        List<String> getCommand = Arrays.asList("bash", "-c",
                "echo testpass | iinit; ils ../test_user");
        doReturn(List.of("user_success")).when(irodsService).runProcess(any());
        doReturn("testpass").when(config).getPassword();
        doThrow(IrodsException.class).when(irodsService).runProcess(getCommand);

        List<String> addCommand = Arrays.asList("bash", "-c",
                "echo testpass | iinit; iadmin mkuser test_user rodsuser");

        irodsService.addIrodsUser("test_user");
        verify(irodsService, times(1)).runProcess(getCommand);
        verify(irodsService, times(1)).runProcess(addCommand);
    }

    @Test
    void testAddUserAlreadyExists() throws IrodsException, IOException,
            InterruptedException {
        doReturn(List.of("user_success")).when(irodsService).runProcess(any());
        doReturn("testpass").when(config).getPassword();

        assertThrows(ResourceAlreadyExistsException.class, () -> irodsService.addIrodsUser("test_user"));
    }

    @Test
    void testGrantAccessToUser() throws IrodsException,
            IOException, InterruptedException, ResourceAlreadyExistsException {
        List<String> ownCmd = Arrays.asList(
                "bash", "-c",
                "echo testpass | iinit; ils -A ../test_user"
        );

        List<String> ownResult = Arrays.asList("first_line", "ACL - test_user#TUG:own");
        doReturn(ownResult).when(irodsService).runProcess(ownCmd);
        doReturn(false).when(config).getIpcServices();
        doReturn("TUG").when(config).getZone();
        doReturn("testpass").when(config).getPassword();

        List<String> chmodCmd = Arrays.asList(
                "bash", "-c",
                "echo testpass | iinit; ichmod own rodsadmin /TUG/home/test_user"
        );

        irodsService.grantAccessToUser("test_user");
        verify(irodsService, times(1)).runProcess(chmodCmd);
    }

    @Test
    void testGrantAccessAlreadyExists() throws IrodsException, IOException, InterruptedException, ResourceAlreadyExistsException {
        List<String> ownCmd = Arrays.asList(
                "bash", "-c",
                "echo testpass | iinit; ils -A ../test_user"
        );

        List<String> ownResult = Arrays.asList("first_line", "ACL - test_user#TUG:own  g:rodsadmin#TUG:own");
        doReturn(ownResult).when(irodsService).runProcess(ownCmd);
        doReturn(false).when(config).getIpcServices();
        doReturn("TUG").when(config).getZone();
        doReturn("testpass").when(config).getPassword();

        assertThrows(ResourceAlreadyExistsException.class, () -> irodsService.grantAccessToUser("test_user"));
    }
}
