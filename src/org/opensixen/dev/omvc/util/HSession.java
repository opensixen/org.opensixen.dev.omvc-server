package org.opensixen.dev.omvc.util;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.interfaces.IPO;


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
