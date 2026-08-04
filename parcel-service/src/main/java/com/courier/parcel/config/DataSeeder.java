package com.courier.parcel.config;

import com.courier.parcel.entity.Parcel;
import com.courier.parcel.entity.ParcelStatus;
import com.courier.parcel.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final ParcelRepository parcelRepository;

	@Override
	public void run(String... args) {
		if (parcelRepository.count() > 0) {
			return;
		}

		parcelRepository.save(Parcel.builder()
				.senderName("Nimal Perera")
				.senderAddress("12 Galle Road, Colombo 03")
				.receiverName("Saman Silva")
				.receiverAddress("45 Kandy Road, Colombo 07")
				.weight(2.5)
				.status(ParcelStatus.PENDING)
				.build());

		parcelRepository.save(Parcel.builder()
				.senderName("Ayesha Fernando")
				.senderAddress("88 Marine Drive, Colombo 04")
				.receiverName("Ruwan Jayasuriya")
				.receiverAddress("7 Hill Street, Kandy")
				.weight(1.2)
				.status(ParcelStatus.PENDING)
				.build());

		log.info("Seeded {} demo parcels", parcelRepository.count());
	}
}
