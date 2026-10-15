package com.ninad.library.domain;

public sealed interface LoanResult permits LoanGranted, LoanRejected {
}
