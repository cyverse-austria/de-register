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

        irodsService.addIrodsUser("test_user");
        verify(irodsService, times(1)).runProcess(getCommand);
    }
}
