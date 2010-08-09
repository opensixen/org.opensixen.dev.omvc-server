package org.opensixen.dev.omvc.model;

import org.opensixen.dev.omvc.util.HSession;

public abstract class PO {

	public boolean save()	{
		HSession.save(this);
		return true;
	}
	
}
