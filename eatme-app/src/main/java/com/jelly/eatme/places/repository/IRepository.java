package com.jelly.eatme.places.repository;

import com.jelly.eatme.places.domain.PlaceList;
import com.jelly.eatme.places.domain.key.IPrimaryKey;
import com.jelly.eatme.places.repository.service.ServiceException;

public interface IRepository<T> {

    PlaceList read(final IPrimaryKey key) throws ServiceException;

}
