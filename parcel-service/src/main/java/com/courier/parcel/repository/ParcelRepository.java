package com.courier.parcel.repository;

import com.courier.parcel.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
}
