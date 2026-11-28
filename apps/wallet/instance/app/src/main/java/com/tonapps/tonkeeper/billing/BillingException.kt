package com.tonapps.tonkeeper.billing

class BillingException(val result: BillingResult) : Exception(result.debugMessage)
