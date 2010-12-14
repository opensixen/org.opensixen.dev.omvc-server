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

package org.opensixen.dev.omvc.util;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.interfaces.IPO;

/**
 * HSession
 * Helper for Hibernate 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
public class HSession {
	private static Logger s_log = Logger.getLogger(HSession.class);

	/** Hibernate Session Factory */
	private static final SessionFactory sessionFactory;

	/**
	 * Inicio estatico del factory de Hibernate
	 */
	static {
		try {
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * Develve la sesion de Hibernate
	 * @return
	 * @throws HibernateException
	 */
	public static Session getSession()
	throws HibernateException {
		return sessionFactory.openSession();
	}

	/**
	 * Devuelve un Criteria con los filtros de seguridad apropiados
	 * @param persistentClass
	 * @return
	 */
	public static Criteria getCriteria(Class persistentClass)	{
		Session sess = getSession();
		return getCriteria(sess, persistentClass);
	}
	
	/**
	 * Devuelve un Criteria con los filtros de seguridad apropiados
	 * @param sess
	 * @param persistentClass
	 * @return
	 */
	public static Criteria getCriteria(Session sess, Class persistentClass)	{		
		if (sess == null)	{
			sess = getSession();
		}
		
		Criteria crit = sess.createCriteria(persistentClass);
		
		
		return crit;
	}
		
	/**
	 * Graba un objeto en base de datos..
	 * @param o
	 */
	public static boolean save(Object o) {
		try {
			Session sess = HSession.getSession();
			sess.beginTransaction();
			save(sess, o);
			sess.getTransaction().commit();
			s_log.info("Guardado con exito.");
			return true;
		}
		catch (Exception e)	{
			e.printStackTrace();
			s_log.error("No se ha podido grabar el objeto: " + o .getClass().getCanonicalName(), e);
			return false;
		}
	}

	/**
	 * Graba un objeto en base de datos..
	 * @param o Objeto a guardar
	 * @param sess Session
	 */
	public static boolean save(Session sess, Object o)	throws HibernateException {
		try {	
			sess.saveOrUpdate(o);
			s_log.info("Guardado con exito.");
			return true;
		}
		catch (HibernateException e)	{
			s_log.error("No se ha podido grabar el objeto", e);
			return false;
		}
	 }		
	
	/**
	 * Borra un objeto en base de datos..
	 * @param o Objeto a borrar
	 */
	public static void delete(Object o)	{
		try {
			Session sess = HSession.getSession();
			sess.beginTransaction();
			delete(sess, o);
			sess.getTransaction().commit();
		}
		catch (Exception e)	{
			e.printStackTrace();
		}
	}
	/**
	 * Borra un objeto en base de datos..
	 * @param sess Session
	 * @param o Objeto a borrar
	 */
	public static void delete(Session sess, Object o)	{
		sess.delete(o);
	}

	/**
	 * Devuelve el objeto con ID dado
	 * @param <T>
	 * @param clazz
	 * @param id
	 * @return
	 */
	public static <T extends IPO> T get(Class<T> clazz, int id)	{
		Criteria crit = getCriteria(clazz);
		crit.add(Restrictions.idEq(new Integer(id)));
		return (T) crit.uniqueResult();
	}
		
}
