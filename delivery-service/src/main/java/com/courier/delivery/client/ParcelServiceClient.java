package com.courier.delivery.client;

public interface ParcelServiceClient {

	void updateStatus(String parcelId, String parcelStatus);
}
