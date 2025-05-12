package com.zhsaidk.config;

import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.util.*;

public class CustomLookupStrategy implements LookupStrategy {

    private final JdbcTemplate jdbcTemplate;
    private final AclCache aclCache;
    private final AclAuthorizationStrategy aclAuthorizationStrategy;
    private final AuditLogger auditLogger;
    private final ConversionService conversionService;
    private final DefaultPermissionFactory permissionFactory;

    public CustomLookupStrategy(DataSource dataSource, AclCache aclCache, AclAuthorizationStrategy aclAuthorizationStrategy, AuditLogger auditLogger, ConversionService conversionService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.aclCache = aclCache;
        this.aclAuthorizationStrategy = aclAuthorizationStrategy;
        this.auditLogger = auditLogger;
        this.conversionService = conversionService;
        this.permissionFactory = new DefaultPermissionFactory();
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) {
        // Отладка текущего пользователя и его ролей
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Current user: " + (auth != null ? auth.getName() : "anonymous") + ", authorities: " + (auth != null ? auth.getAuthorities() : "none"));

        String sql = "select acl_object_identity.object_id_identity, acl_entry.ace_order, acl_object_identity.id as acl_id, " +
                "acl_object_identity.parent_object, acl_object_identity.entries_inheriting, acl_entry.id as ace_id, " +
                "acl_entry.mask, acl_entry.granting, acl_entry.audit_success, acl_entry.audit_failure, " +
                "acl_sid.principal as ace_principal, acl_sid.sid as ace_sid, acli_sid.principal as acl_principal, " +
                "acli_sid.sid as acl_sid, acl_class.class, acl_class.class_id_type " +
                "from acl_object_identity " +
                "left join acl_sid acli_sid on acli_sid.id = acl_object_identity.owner_sid " +
                "left join acl_class on acl_class.id = acl_object_identity.object_id_class " +
                "left join acl_entry on acl_object_identity.id = acl_entry.acl_object_identity " +
                "left join acl_sid on acl_entry.sid = acl_sid.id " +
                "where (acl_object_identity.object_id_identity = ? and acl_class.class = ?) " +
                "order by acl_object_identity.object_id_identity asc, acl_entry.ace_order asc";

        Map<ObjectIdentity, Acl> acls = new HashMap<>();
        for (ObjectIdentity oid : objects) {
            System.out.println("Looking up ACL for: " + oid.getType() + ", ID: " + oid.getIdentifier() + ", ID type: " + oid.getIdentifier().getClass().getName());
            try {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, oid.getIdentifier().toString(), oid.getType());
                System.out.println("SQL results for " + oid.getType() + " ID " + oid.getIdentifier() + ": " + results);
                if (!results.isEmpty()) {
                    // Создаем AclImpl
                    MutableAcl acl = new AclImpl(
                            new ObjectIdentityImpl(oid.getType(), oid.getIdentifier()),
                            (Long) results.get(0).get("acl_id"),
                            aclAuthorizationStrategy,
                            auditLogger
                    );

                    // Обработка owner_sid
                    boolean aclPrincipal = (Boolean) results.get(0).get("acl_principal");
                    String aclSid = (String) results.get(0).get("acl_sid");
                    Sid ownerSid = aclPrincipal ? new PrincipalSid(aclSid) : new GrantedAuthoritySid(aclSid);
                    acl.setOwner(ownerSid);
                    System.out.println("Owner SID: " + ownerSid);

                    // Устанавливаем entries_inheriting после добавления ACE
                    boolean entriesInheriting = (Boolean) results.get(0).get("entries_inheriting");

                    // Обработка parent_object
                    Long parentId = (Long) results.get(0).get("parent_object");
                    if (parentId != null) {
                        Acl cachedParent = aclCache.getFromCache(parentId);
                        if (cachedParent != null) {
                            acl.setParent((MutableAcl) cachedParent);
                        } else {
                            String parentClass = jdbcTemplate.queryForObject(
                                    "select acl_class.class from acl_class join acl_object_identity on acl_class.id = acl_object_identity.object_id_class where acl_object_identity.id = ?",
                                    String.class, parentId);
                            ObjectIdentity parentOid = new ObjectIdentityImpl(parentClass, parentId.toString());
                            Map<ObjectIdentity, Acl> parentAcls = readAclsById(Collections.singletonList(parentOid), sids);
                            if (!parentAcls.isEmpty()) {
                                MutableAcl parentAcl = (MutableAcl) parentAcls.get(parentOid);
                                acl.setParent(parentAcl);
                                aclCache.putInCache(parentAcl);
                            }
                        }
                    }

                    // Обработка ACE (Access Control Entries)
                    boolean hasAce = false;
                    for (Map<String, Object> row : results) {
                        Long aceId = (Long) row.get("ace_id");
                        if (aceId != null) {
                            hasAce = true;
                            boolean acePrincipal = (Boolean) row.get("ace_principal");
                            String aceSid = (String) row.get("ace_sid");
                            Sid entrySid = acePrincipal ? new PrincipalSid(aceSid) : new GrantedAuthoritySid(aceSid);
                            AccessControlEntryImpl ace = new AccessControlEntryImpl(
                                    aceId,
                                    acl,
                                    entrySid,
                                    permissionFactory.buildFromMask((Integer) row.get("mask")),
                                    (Boolean) row.get("granting"),
                                    (Boolean) row.get("audit_success"),
                                    (Boolean) row.get("audit_failure")
                            );
                            acl.insertAce(acl.getEntries().size(), ace.getPermission(), entrySid, ace.isGranting());
                            System.out.println("Added ACE: SID=" + entrySid + ", mask=" + row.get("mask"));
                        }
                    }

                    if (!hasAce) {
                        Permission adminPermission = permissionFactory.buildFromMask(BasePermission.ADMINISTRATION.getMask());
                        acl.insertAce(0, adminPermission, ownerSid, true);
                        System.out.println("Added default ADMINISTRATION ACE for owner: " + ownerSid);
                    }

                    acl.setEntriesInheriting(entriesInheriting);

                    acls.put(oid, acl);
                    aclCache.putInCache(acl);
                } else {
                    System.out.println("No ACL found for: " + oid.getType() + ", ID: " + oid.getIdentifier());
                }
            } catch (Exception e) {
                System.out.println("Failed to lookup ACL for: " + oid.getType() + ", ID: " + oid.getIdentifier() + ", error: " + e.getMessage());
                throw new RuntimeException("ACL lookup failed", e);
            }
        }

        return acls;
    }
}