sealed class WalletState {
    object NotConnected : WalletState()
    
    data class Connected(
        val address: String,
        val balance: Double,
        val totalStaked: Double,
        val totalEarned: Double
    ) : WalletState()
} 