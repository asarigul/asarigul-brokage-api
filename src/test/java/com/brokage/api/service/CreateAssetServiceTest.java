package com.brokage.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.brokage.api.exception.InvalidArgumentException;
import com.brokage.api.exception.SecurityException;

@ExtendWith(MockitoExtension.class)
public class CreateAssetServiceTest {
	
	@Spy
	@InjectMocks
	private AssetService assetService;
	
	@Test
	void createAsset_shouldFail_whenCallerIsNotAdmin() {
		doReturn(false).when(assetService).isAdmin();
		
		assertThrows(SecurityException.class, () -> {
			assetService.createAsset(1L, "USD", BigDecimal.ONE, BigDecimal.ONE);
		});
	}
	
	@Test
	void createAsset_shouldFail_whenUsableSizeIsBiggerThanSize() {
		doReturn(true).when(assetService).isAdmin();
		
		assertThrows(InvalidArgumentException.class, () -> {
			assetService.createAsset(1L, "USD", BigDecimal.ONE, BigDecimal.TEN);
		});
	}
}
