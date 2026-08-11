package com.courier.parcel.repository;

import com.courier.parcel.entity.Parcel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ParcelRepository extends MongoRepository<Parcel, String> {
}
