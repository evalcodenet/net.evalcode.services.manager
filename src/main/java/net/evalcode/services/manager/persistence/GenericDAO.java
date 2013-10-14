package net.evalcode.services.manager.persistence;


import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


/**
 * GenericDAO
 *
 * @author carsten.schipke@gmail.com
 */
public final class GenericDAO<T>
{
  // MEMBERS
  private final Class<T> clazz;
  private final Entity entity;
  private final EntityManager entityManager;


  // CONSTRUCTION
  private GenericDAO(final EntityManager entityManager, final Class<T> clazz)
  {
    super();

    this.clazz=clazz;
    this.entity=clazz.getAnnotation(Entity.class);

    this.entityManager=entityManager;
  }


  // STATIC ACCESSORS
  public static <T> GenericDAO<T> get(final EntityManager entityManager, final Class<T> clazz)
  {
    return new GenericDAO<T>(entityManager, clazz);
  }


  // ACCESSORS/MUTATORS
  public T save(final T record)
  {
    entityManager.persist(record);

    return record;
  }

  public List<T> findAll()
  {
    if(null==entity || null==entity.name())
      throw new IllegalArgumentException("This operation requires an @Entity with defined name.");

    final TypedQuery<T> query=entityManager.createQuery(
      String.format("SELECT e FROM %1$s e", entity.name()), clazz
    );

    return query.getResultList();
  }

  public T findByPK(final Long primaryKey)
  {
    return entityManager.find(clazz, primaryKey);
  }


  // list behavior
  public T findFirst()
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }

  public T findLast()
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }

  public T moveUp(final T item)
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }

  public T moveDown(final T item)
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }

  public T moveAtTop(final T item)
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }

  public T moveAtBottom(final T item)
  {
    return null;
  }


  // tree behavior
  public T findParent(final T node)
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }

  public List<T> findChildren(final T node)
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }

  public List<T> findSiblings(final T node)
  {
    // TODO Implement.

    throw new UnsupportedOperationException();
  }
}
