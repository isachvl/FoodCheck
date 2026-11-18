package com.example.foodcheck

sealed class ScanType {
    data object ScanQr : ScanType()
    data object ScanText : ScanType()
    data object ScanCard : ScanType()
    data object None : ScanType()
}