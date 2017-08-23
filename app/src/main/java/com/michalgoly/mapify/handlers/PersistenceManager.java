package com.michalgoly.mapify.handlers;

import com.michalgoly.mapify.model.PolylineWrapper;
import com.michalgoly.mapify.model.User;

/**
 * This interface describes methods necessary for the Mapify persistence layer
 */
public interface PersistenceManager {

    /**
     * Persists the User
     *
     * @param user User - The User to be persisted, cannot be null
     */
    void insert(User user);

    /**
     * Updates the existing record of the user provided
     *
     * @param user User - The user to update, cannot be null
     */
    void update(User user);

    /**
     * Retrieves the user by the id provided
     *
     * @param id String - The id of the persisted user to retrieve, cannot be null
     * @return User - The User with the id specified
     */
    User select(String id);

    /**
     * Deletes the user specified, cannot be null
     *
     * @param user User - The User to delete from the persistence layer
     */
    void delete(User user);

    /**
     * Persists the PolylineWrapper
     *
     * @param pw PolylineWrapper - The PolylineWrapper to be persisted, cannot be null
     */
    void insert(PolylineWrapper pw);

    /**
     * Updates the existing record of the PolylineWrapper provided
     *
     * @param pw PolylineWrapper - The PolylineWrapper to update, cannot be null
     */
    void update(PolylineWrapper pw);

    /**
     * Retrieves the PolylineWrapper by the id provided
     *
     * @param id Long - The id of the persisted PolylineWrapper to retrieve, cannot be null
     * @return PolylineWrapper - The PolylineWrapper with the id specified
     */
    PolylineWrapper select(Long id);

    /**
     * Deletes the PolylineWrapper specified, cannot be null
     *
     * @param pw PolylineWrapper - The PolylineWrapper to delete from the persistence layer
     */
    void delete(PolylineWrapper pw);

}
