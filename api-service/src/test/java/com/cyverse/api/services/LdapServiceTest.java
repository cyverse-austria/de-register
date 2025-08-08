package com.cyverse.api.services;

import com.cyverse.api.config.LdapServiceConfig;
import com.cyverse.api.exceptions.ResourceAlreadyExistsException;
import com.cyverse.api.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.naming.NamingException;
import javax.naming.directory.*;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class LdapServiceTest {
    @Mock
    private LdapServiceConfig config;

    @Spy
    @InjectMocks
    LdapService ldapService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddLdapUser() throws NamingException, NoSuchAlgorithmException,
            ResourceAlreadyExistsException {
        UserModel user = new UserModel();
        user.setUsername("test_user");
        user.setEmail("test_user@test.com");
        user.setFirstName("test");
        user.setLastName("user");

        doReturn("dc=example,dc=org").when(config).getBaseDN();
        doReturn("testpass").when(config).getFirstLoginPassword();
        doReturn("{SSHA}testpasshashed1234").when(ldapService).generateSSHAHash("testpass");

        String expectedEntryDN = "uid=test_user,ou=People,dc=example,dc=org";
        Attributes attrs = buildAttributes(user);

        doNothing().when(ldapService).addEntryDN(expectedEntryDN, attrs);

        ldapService.addLdapUser(user);
        verify(ldapService, times(1)).addEntryDN(expectedEntryDN, attrs);
    }

    @Test
    void testAddLdapUserAlreadyExists() throws NamingException, NoSuchAlgorithmException {
        UserModel user = new UserModel();
        user.setUsername("test_user");
        user.setEmail("test_user@test.com");
        user.setFirstName("test");
        user.setLastName("user");

        doReturn("dc=example,dc=org").when(config).getBaseDN();
        doReturn("testpass").when(config).getFirstLoginPassword();
        doReturn("{SSHA}testpasshashed1234").when(ldapService).generateSSHAHash("testpass");

        String expectedEntryDN = "uid=test_user,ou=People,dc=example,dc=org";
        Attributes attrs = buildAttributes(user);

        NamingException e = new NamingException("Entry Already Exists");
        doThrow(e).when(ldapService).addEntryDN(expectedEntryDN, attrs);

        assertThrows(ResourceAlreadyExistsException.class, () -> ldapService.addLdapUser(user));
    }

    @Test
    void testAddLdapUserToGroup() throws ResourceAlreadyExistsException, NamingException {
        String testUser = "test_user";
        String group = "everyone";

        doReturn("everyone").when(config).getEveryoneGroup();
        doReturn("dc=example,dc=org").when(config).getBaseDN();

        String expectedGroupDN = "cn=everyone,ou=Groups,dc=example,dc=org";
        doNothing().when(ldapService).modifyAttrs(any(), any());

        ldapService.addLdapUserToGroup(testUser, group);

        verify(ldapService, times(1)).modifyAttrs(
                eq(expectedGroupDN),
                argThat(mods ->
                mods.length == 1 &&
                        mods[0].getModificationOp() == DirContext.ADD_ATTRIBUTE &&
                        "memberUid".equals(mods[0].getAttribute().getID()) &&
                        mods[0].getAttribute().contains("test_user")));
    }

    @Test
    void testAddLdapUserToGroupAlreadyExists() throws NamingException {
        String testUser = "test_user";
        String group = "everyone";

        doReturn("everyone").when(config).getEveryoneGroup();
        doReturn("dc=example,dc=org").when(config).getBaseDN();

        String expectedGroupDN = "cn=everyone,ou=Groups,dc=example,dc=org";
        doThrow(AttributeInUseException.class).when(ldapService).modifyAttrs(eq(expectedGroupDN),
                argThat(mods ->
                mods.length == 1 &&
                        mods[0].getModificationOp() == DirContext.ADD_ATTRIBUTE &&
                        "memberUid".equals(mods[0].getAttribute().getID()) &&
                        mods[0].getAttribute().contains("test_user")));

        assertThrows(ResourceAlreadyExistsException.class, () -> ldapService.addLdapUserToGroup(testUser, group));
    }

    private Attributes buildAttributes(UserModel user) {
        Attribute objClass = new BasicAttribute("objectClass");
        objClass.add("inetOrgPerson");
        objClass.add("posixAccount");
        objClass.add("shadowAccount");

        Attributes attrs = new BasicAttributes(true);

        attrs.put(objClass);
        attrs.put("givenName", user.getFirstName());
        attrs.put("sn", user.getFirstName());
        attrs.put("cn", user.getFirstName() + " " + user.getLastName());
        attrs.put("uid", user.getUsername());
        attrs.put("mail", user.getEmail());
        attrs.put("gidNUmber", "10013");
        attrs.put("homeDirectory", "/home/" + user.getUsername());
        attrs.put("loginShell", "/bin/bash");
        attrs.put("userPassword", "{SSHA}testpasshashed1234");
        attrs.put("title", "University/College Staff");
        attrs.put("o", "Graz University of Technology");

        return attrs;
    }
}
