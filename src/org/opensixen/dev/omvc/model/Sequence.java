package org.opensixen.dev.omvc.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.util.HSession;

@Entity
@TableGenerator(name = "Sequence_SEQ", pkColumnValue = "sequence_ID", table = "ID_SEQUENCE", pkColumnName = "pkcolname", valueColumnName = "value",  initialValue = 1000, allocationSize = 1)
@Table(name="sequence", uniqueConstraints = {@UniqueConstraint(columnNames="sequence_ID"), @UniqueConstraint(columnNames="tableName")})
public class Sequence extends PO implements Serializable{		
	private static final long serialVersionUID = 1L;	
	
	/** 
	 * Valor por defecto en el que empezaran las nuevas tablas	
	 * En Adempiere empiezan por 50.000
	 * Nosotros empezaremos por 100.000 mas, 150.000
	 * Las personalizaciones de usuario empiezan por 1.000.000 
	 */
	private static final int  initialValue = 150000;

	private int sequence_ID;
	private String tableName;
	private int current_ID;
	private Date updated;
	
	
	
	
	public Sequence() {
		super();
		// TODO Auto-generated constructor stub
	}
		
	
	public Sequence(String tableName) {
		super();
		this.tableName = tableName;
		this.current_ID = initialValue;
	}


	@Id
	@Column (name = "sequence_ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "Sequence_SEQ")
	public int getSequence_ID() {
		return sequence_ID;
	}
	
	public void setSequence_ID(int sequence_ID) {
		this.sequence_ID = sequence_ID;
	}
	
	@Column(name="tableName", unique=true, nullable=false)
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public int getCurrent_ID() {
		return current_ID;
	}
	
	public void setCurrent_ID(int current_ID) {
		this.current_ID = current_ID;
	}
	
	public Date getUpdated() {
		return updated;
	}
	
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	
	@Transient
	public int getNext_ID()	{
		current_ID++;
		if (save())	{
			return getCurrent_ID();
		}
		else	{
			return -1;
		}			
	}

	public static Sequence getSequence(String tableName)	{
		Criteria crit = HSession.getCriteria(Sequence.class);
		crit.add(Restrictions.eq("tableName", tableName));
		
		Sequence seq = (Sequence) crit.uniqueResult();
		
		if (seq == null)	{
			seq = new Sequence(tableName);
		}
		
		return seq;
	}
}
