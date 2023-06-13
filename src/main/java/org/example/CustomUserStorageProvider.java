package org.example;

import com.msb.ibs.common.crypto.MD5Password;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import javax.persistence.EntityManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class CustomUserStorageProvider implements UserLookupProvider, UserStorageProvider,
        CredentialInputValidator,
        UserQueryProvider {

    private KeycloakSession ksession;
    private ComponentModel model;

    protected EntityManager em;


    public CustomUserStorageProvider(KeycloakSession ksession, ComponentModel model) {
        this.ksession = ksession;
        this.model = model;
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        StorageId sid = new StorageId(userModel.getId());
        String username = sid.getExternalId();
        String pwd = credentialInput.getChallengeResponse();
        String loginPwd = MD5Password.MD5Pwd(pwd);
        log.info("isValid " + username + " " + loginPwd);
//
        try ( Connection c = DbUtil.getConnection(this.model)) {
//            PreparedStatement st = c.prepareStatement("select * from BB_USER_INFO where user_name = ? AND pwd_login = ?");
            PreparedStatement st = c.prepareStatement("select * from BB_USER_INFO where user_name = ?");
            st.setString(1, username);
//            st.setString(2, loginPwd);
            st.execute();
            ResultSet rs = st.getResultSet();
            if ( rs.next()) {
               return true;
            }
            else {
                return false;
            }
        }
        catch(SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(),ex);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(RealmModel realmModel, String s) {
        String externalId = StorageId.externalId(s);

        return getUserByUsername(realmModel, externalId);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        log.info("[I57] supportsCredentialType({})",credentialType);
        return PasswordCredentialModel.TYPE.endsWith(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        log.info("[I57] isConfiguredFor(realm={},user={},credentialType={})",realm.getName(), user.getUsername(), credentialType);
        // In our case, password is the only type of credential, so we allways return 'true' if
        // this is the credentialType
        return supportsCredentialType(credentialType);
    }

//    @Override
//    public UserModel getUserByUsername(RealmModel realmModel, String s) {
//        TypedQuery<User> query = em.createNamedQuery("getUserByUsername", User.class);
//        query.setParameter("username", s);
//        List<User> result = query.getResultList();
//        return mapUser(realmModel, new CustomUser(ksession, realmModel, model, result.get(0).getUserName(), " ", "fsd", "fsd", null));
//    }

    @Override
    public UserModel getUserByUsername(RealmModel realmModel, String s) {
//        log.info("getUserByUsername " + s);
        try ( Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement("select user_id, user_name, email, login_pwd from BB_USER_INFO where user_name = ?");
            st.setString(1, s);
            st.execute();
            ResultSet rs = st.getResultSet();
            if ( rs.next()) {
                return mapUser(realmModel,rs);
            }
            else {
                return null;
            }
        }
        catch(SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(),ex);
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realmModel, String s) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, String s, Integer integer, Integer integer1) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> map, Integer integer, Integer integer1) {
        return null;
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
        return null;
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return null;
    }

    private UserModel mapUser(RealmModel realm, CustomUser data) {
        CustomUser user = new CustomUser.Builder(ksession, realm, model, data.getUsername())
                .email(data.getEmail())
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
//                .birthDate(data.getBirthDate())
                .build();
        return user;
    }

    private UserModel mapUser(RealmModel realm, ResultSet rs) throws SQLException {
        CustomUser user = new CustomUser.Builder(ksession, realm, model, rs.getString("user_name"))
                .email(rs.getString("email"))
                .build();
        return user;
    }

}
