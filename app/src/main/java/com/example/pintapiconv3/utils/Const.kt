package com.example.pintapiconv3.utils

object Const {

    const val MAX_FAILED_ATTEMPTS = 5

    object MatchStatus {
        const val PENDING = 10
        const val CANCELED = 11
        const val CONFIRMED = 12
        const val IN_COURSE = 13
        const val FINISHED = 14
        const val SUSPENDED = 28
    }
    object ReservationStatus {
        const val PENDING_PAYMENT = 19
        const val PAID = 20
        const val FINISHED = 25
        const val CANCELED = 21
    }

    object PaymentStatus {
        const val PENDING_PAYMENT = 22
        const val PARCIAL_PAID = 23
        const val PAID = 24
        const val MATCH_PLAYED = 26
        const val MATCH_SUSPENDED = 27
        const val MATCH_CANCELED = 29
        const val KICKED_OUT = 30
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

    object PaymentMethod {
        const val CASH = 1
        const val TRANSFER = 2
        const val ONLINE = 3
    }

    object Entities {
        const val USERS = "Usuarios"
        const val RESERVATIONS = "Reservas"
        const val MATCHES = "Partidos"
    }
}