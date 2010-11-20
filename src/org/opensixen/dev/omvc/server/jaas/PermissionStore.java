/**
 * 
 */
package org.opensixen.dev.omvc.server.jaas;

import java.security.Permissions;
import java.security.Principal;
import java.util.HashMap;

import javax.management.RuntimeErrorException;

import org.eclipse.riena.security.authorizationservice.IPermissionStore;
import org.opensixen.dev.omvc.jaas.AnonymousPrincipal;
import org.opensixen.dev.omvc.jaas.DevPrincipal;
import org.opensixen.dev.omvc.jaas.OMVCPermission;

/**
 * 
 * 
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 *
 */
public class PermissionStore implements IPermissionStore {

	
	HashMap<String, Permissions> permissionMap = initPermissions();
	
	
	private static HashMap<String, Permissions> initPermissions()	{
		HashMap<String, Permissions> perm = new HashMap<String, Permissions>();
		
		// anoynous
		Permissions anon = new Permissions();
		anon.add(new OMVCPermission(OMVCPermission.PERM_LISTREV));
		anon.add(new OMVCPermission(OMVCPermission.PERM_LOADREV));		
		perm.put("anonymous", anon);
		
		Permissions dev = new Permissions();
		dev.add(new OMVCPermission(OMVCPermission.PERM_LISTREV));
		dev.add(new OMVCPermission(OMVCPermission.PERM_LOADREV));
		dev.add(new OMVCPermission(OMVCPermission.PERM_SAVEREV));
		dev.add(new OMVCPermission(OMVCPermission.PERM_LOADPO));
		dev.add(new OMVCPermission(OMVCPermission.PERM_LISTPO));
		dev.add(new OMVCPermission(OMVCPermission.PERM_SAVEPO));
		dev.add(new OMVCPermission(OMVCPermission.PERM_GETID));
		perm.put("dev", dev);
		
		return perm;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.riena.security.authorizationservice.IPermissionStore#loadPermissions(java.security.Principal)
	 */
	@Override
	public Permissions loadPermissions(Principal principal) {
		if (principal instanceof DevPrincipal) 	{
			return permissionMap.get("dev");
		}
		else if (principal instanceof AnonymousPrincipal)	{
			return permissionMap.get("anonymous");
		}		
		return new Permissions();
	}

}
