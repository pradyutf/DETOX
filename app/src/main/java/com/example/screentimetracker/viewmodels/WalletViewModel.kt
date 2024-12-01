package com.example.screentimetracker.viewmodels

import WalletState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel() {
    private val _walletState = MutableStateFlow<WalletState>(WalletState.NotConnected)
    val walletState = _walletState.asStateFlow()

    fun connectWallet() {
        // TODO: Implement Web3 wallet connection
        // For now, simulate a connection
        viewModelScope.launch {
            _walletState.value = WalletState.Connected(
                address = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
                balance = 1.5,
                totalStaked = 0.5,
                totalEarned = 0.1
            )
        }
    }

    fun disconnectWallet() {
        viewModelScope.launch {
            _walletState.value = WalletState.NotConnected
        }
    }

    fun changeWallet() {
        // TODO: Implement wallet change functionality
        // For now, just simulate a different wallet
        viewModelScope.launch {
            _walletState.value = WalletState.Connected(
                address = "0x123...abc",
                balance = 2.0,
                totalStaked = 1.0,
                totalEarned = 0.2
            )
        }
    }
} 