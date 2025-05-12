package com.zhsaidk.config;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public class CustomJdbcMutableAclService extends JdbcMutableAclService {

    private static final String INSERT_OBJECT_IDENTITY = "insert into acl_object_identity "
            + "(object_id_class, object_id_identity, owner_sid, entries_inheriting) values (?, ?, ?, ?)";

    private static final String SELECT_OBJECT_IDENTITY_PRIMARY_KEY = "select acl_object_identity.id from acl_object_identity, acl_class "
            + "where acl_object_identity.object_id_class = acl_class.id and acl_class.class=? "
            + "and acl_object_identity.object_id_identity = ?";

    private static final String INSERT_CLASS = "insert into acl_class (class, class_id_type) values (?, ?)";

    private static final String INSERT_ACE = "insert into acl_entry "
            + "(acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) values (?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public CustomJdbcMutableAclService(DataSource dataSource, LookupStrategy lookupStrategy, AclCache aclCache) {
        super(dataSource, lookupStrategy, aclCache);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        setAclClassIdSupported(true);
    }

    @Override
    protected void createObjectIdentity(ObjectIdentity object, Sid owner) {
        Long sidId = createOrRetrieveSidPrimaryKey(owner, true);
        Long classId = createOrRetrieveClassPrimaryKey(object.getType(), true, object.getIdentifier().getClass());
        this.jdbcOperations.update(
                INSERT_OBJECT_IDENTITY,
                classId,
                object.getIdentifier().toString(),
                sidId,
                Boolean.TRUE
        );

        Long aclId = retrieveObjectIdentityPrimaryKey(object);
        if (aclId != null) {
            this.jdbcTemplate.update(
                    INSERT_ACE,
                    aclId,
                    0,
                    sidId,
                    BasePermission.ADMINISTRATION.getMask(),
                    true,
                    false,
                    false
            );
        }
    }

    @Override
    protected Long retrieveObjectIdentityPrimaryKey(ObjectIdentity oid) {
        try {
            return this.jdbcOperations.queryForObject(
                    SELECT_OBJECT_IDENTITY_PRIMARY_KEY,
                    Long.class,
                    oid.getType(),
                    oid.getIdentifier().toString()
            );
        } catch (DataAccessException notFound) {
            return null;
        }
    }

    @Override
    protected Long createOrRetrieveClassPrimaryKey(String type, boolean allowCreate, Class idType) {
        List<Map<String, Object>> result = this.jdbcOperations.queryForList(
                "select id, class_id_type from acl_class where class=?",
                type
        );

        if (!result.isEmpty()) {
            Map<String, Object> row = result.get(0);
            String storedType = (String) row.get("class_id_type");
            if (storedType == null || !storedType.equals(idType.getName())) {
                throw new IllegalStateException("class_id_type mismatch for " + type);
            }
            return (Long) row.get("id");
        }

        if (allowCreate) {
            String classIdType = idType.getName();
            this.jdbcOperations.update(INSERT_CLASS, type, classIdType);
            return this.jdbcOperations.queryForObject(
                    "SELECT currval(pg_get_serial_sequence('acl_class', 'id'))",
                    Long.class
            );
        }

        return null;
    }

    @Override
    public MutableAcl createAcl(ObjectIdentity objectIdentity) {
        return super.createAcl(objectIdentity);
    }
}