package com.example.pintapiconv3.utils

object Const {

    const val MAX_FAILED_ATTEMPTS = 5

    object MatchStatus {
        const val PENDING = 10
        const val CANCELED = 11
        const val CONFIRMED = 12
        const val IN_COURSE = 13
        const val FINISHED = 14
    }
    object ReservationStatus {
        const val PENDING_PAYMENT = 19
        const val PAID = 20
        const val CANCELED = 21
    }
    object PaymentStatus {
        const val PENDING_PAYMENT = 22
        const val PARCIAL_PAID = 23
        const val PAID = 24
    }

    object FieldStatus {
        const val OPEN = 6
        const val CLOSED = 7
        const val OUT_OF_SERVICE = 8
        const val ELIMINATED = 9
    }

    object AccountStatus {
        const val NOT_VERIFIED = 1
        const val VERIFIED = 2
        const val DELETED = 3
        const val SUSPENDED = 4
        const val BLOCKED = 5
    }

    object Gender {
        const val MALE = 1
        const val FEMALE = 2
        const val OTHER = 3
    }
}