 /******* BEGIN LICENSE BLOCK *****
 * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
 *
 * Los contenidos de este fichero están sujetos a la Licencia
 * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
 * usar este fichero, excepto bajo las condiciones que otorga dicha 
 * Licencia y siempre de acuerdo con el contenido de la presente. 
 * Una copia completa de las condiciones de de dicha licencia,
 * traducida en castellano, deberá estar incluida con el presente
 * programa.
 * 
 * Adicionalmente, puede obtener una copia de la licencia en
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Este fichero es parte del programa opensiXen.
 *
 * OpensiXen es software libre: se puede usar, redistribuir, o
 * modificar; pero siempre bajo los términos de la Licencia 
 * Pública General de GNU, tal y como es publicada por la Free 
 * Software Foundation en su versión 2.0, o a su elección, en 
 * cualquier versión posterior.
 *
 * Este programa se distribuye con la esperanza de que sea útil,
 * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
 * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
 * los detalles de la Licencia Pública General GNU para obtener una
 * información más detallada. 
 *
 * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
 * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
 * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
 *
 * El desarrollador/es inicial/es del código es
 *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
 *  Indeos Consultoria S.L. - http://www.indeos.es
 *
 * Contribuyente(s):
 *  Eloy Gómez García <eloy@opensixen.org> 
 *
 * Alternativamente, y a elección del usuario, los contenidos de este
 * fichero podrán ser usados bajo los términos de la Licencia Común del
 * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
 * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
 * copia completa de las condiciones de dichas licencias, traducida en 
 * castellano, deberán de estar incluidas con el presente programa.
 * Adicionalmente, es posible obtener una copia original de dichas 
 * licencias en su versión original en
 *  http://www.opensource.org/licenses/cddl1.php  y en  
 *  http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * Si el usuario desea el uso de SU versión modificada de este fichero 
 * sólo bajo los términos de una o más de las licencias, y no bajo los 
 * de las otra/s, puede indicar su decisión borrando las menciones a la/s
 * licencia/s sobrantes o no utilizadas por SU versión modificada.
 *
 * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
 * puede utilizar este fichero bajo cualquiera de las tres licencias que 
 * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
 *
 * ***** END LICENSE BLOCK ***** */

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

/**
 * 
 * 
 * 
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 *
 */
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
