package com.wwf.shrimp.application.models;

import java.util.ArrayList;
import java.util.List;

/**
 * A role for an entity.
 * 
 * A role would define permissions that are given to some 
 * specific annotation.
 * For example we could create an "Administrator" role
 * which would give the user the ability to read all documents 
 * in an organization.
 * 
 * @author AleaActaEst
 *
 */
public class Role extends LookupEntity {
	
	public static final String ROLE_NAME_SUPER_ADMIN = "Super Admin";
	public static final String ROLE_NAME_MATRIX_ADMIN = "Matrix Admin";
	public static final String ROLE_NAME_ORG_ADMIN = "Org Admin";
	public static final String ROLE_NAME_USER = "User";
	public static final String ROLE_NAME_GENERAL_USER = "General";

	
	private List<Permission> permissions = new ArrayList<Permission>();

	/**
	 * @return the permissions
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}
}
