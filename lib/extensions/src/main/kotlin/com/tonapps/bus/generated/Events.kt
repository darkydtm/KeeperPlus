package com.tonapps.bus.generated

/**
 * Functional routing values shared by wallet features.
 *
 * These enums retain stable serialized keys used by navigation and protocol
 * state; analytics event execution is intentionally not part of this API.
 */
interface Events {

    interface AssetScreen {

        enum class AssetScreenFrom(val key: String) {
            WalletScreen("wallet_screen"),
            DeepLink("deep_link"),
            QrCode("qr_code"),
            TradeScreen("trade_screen")
        }
    }


    interface BatteryNative {

        enum class BatteryNativeFrom(val key: String) {
            Wallet("wallet"),
            Settings("settings"),
            TronFees("tron_fees"),
            InsufficientFunds("insufficient_funds"),
            Deeplink("deeplink"),
            Send("send"),
            BatteryBanner("battery_banner")
        }

        enum class BatteryNativeSize(val key: String) {
            Custom("custom"),
            Small("small"),
            Medium("medium"),
            Large("large")
        }

        enum class BatteryNativeType(val key: String) {
            Crypto("crypto"),
            Fiat("fiat")
        }
    }


    interface DappBrowser {

        enum class DappAppClickFrom(val key: String) {
            Banner("banner"),
            Browser("browser"),
            BrowserSearch("browser_search"),
            BrowserConnected("browser_connected"),
            Push("push"),
            Sidebar("sidebar"),
            DeepLink("deep_link"),
            History("history"),
            Collectibles("collectibles"),
            Tonconnect("tonconnect"),
            EarnFeatured("earn_featured"),
            EarnYourVaults("earn_your_vaults"),
            EarnApy("earn_apy"),
            Dapp("dapp"),
            Shortcut("shortcut"),
            Other("other")
        }

        enum class DappBrowserAssetChain(val key: String) {
            Ton("ton"),
            Eth("eth"),
            Base("base"),
            Arb("arb"),
            Bsc("bsc"),
            Polygon("polygon"),
            Avax("avax"),
            Robinhood("robinhood"),
            Sol("sol"),
            Tron("tron"),
            Btc("btc"),
            Ltc("ltc"),
            Doge("doge"),
            Bch("bch"),
            Lighter("lighter"),
            Multichain("multichain")
        }

        enum class DappBrowserOpenFrom(val key: String) {
            Wallet("wallet"),
            History("history"),
            DeepLink("deep_link"),
            Story("story"),
            ConnectedApps("connected_apps"),
            Other("other")
        }

        enum class DappBrowserType(val key: String) {
            Explore("explore"),
            Connected("connected")
        }

        enum class DappSharingCopyFrom(val key: String) {
            Share("share"),
            CopyLink("copy_link")
        }
    }


    interface DepositFlow {

        enum class DepositFlowDepositScreen(val key: String) {
            AddFunds("add_funds"),
            BuyCrypto("buy_crypto")
        }

        enum class DepositFlowFrom(val key: String) {
            WalletScreen("wallet_screen"),
            JettonScreen("jetton_screen"),
            DeepLink("deep_link"),
            QrCode("qr_code"),
            HistoryScreen("history_screen"),
            Browser("browser"),
            Collectibles("collectibles"),
            Perps("perps"),
            Walletconnect("walletconnect"),
            Tonconnect("tonconnect"),
            Staking("staking"),
            Migration("migration"),
            Other("other")
        }

        enum class DepositFlowProviderMode(val key: String) {
            Headless("headless"),
            Merchant("merchant")
        }

        enum class DepositFlowWalletMode(val key: String) {
            Multi("multi"),
            Single("single")
        }
    }


    interface EarnFlow {

        enum class EarnEntryViewFrom(val key: String) {
            EarnFeatured("earn_featured"),
            EarnYourVaults("earn_your_vaults"),
            EarnApy("earn_apy")
        }
    }


    interface LedgerFlow {

        enum class LedgerDeviceViewFrom(val key: String) {
            Settings("settings"),
            WalletHeader("wallet_header"),
            SetupSection("setup_section")
        }

        enum class LedgerDiscoveryErrorFrom(val key: String) {
            Import("import"),
            Migration("migration"),
            Repair("repair"),
            AddNetworks("add_networks")
        }

        enum class LedgerMigrationStartFrom(val key: String) {
            Tooltip("tooltip"),
            Settings("settings"),
            Import("import"),
            Startup("startup")
        }
    }


    interface MysteryRaffle {

        enum class MysteryRaffleSource(val key: String) {
            WalletMain("wallet_main"),
            WalletsList("wallets_list"),
            Trade("trade"),
            SwapPromo("swap_promo"),
            DeepLink("deep_link")
        }
    }


    interface Perps {

        enum class PerpsEntryPoint(val key: String) {
            MainBanner("main_banner"),
            MainCarousel("main_carousel"),
            MainPositions("main_positions"),
            AssetPage("asset_page"),
            Search("search"),
            TradeTab("trade_tab"),
            Loyalty("loyalty"),
            Other("other")
        }

        enum class PerpsSourceScreen(val key: String) {
            PerpsHome("perps_home"),
            OpenPosition("open_position"),
            AddToPosition("add_to_position"),
            AddMargin("add_margin")
        }
    }


    interface RedOperations {

        enum class RedOperationsFlow(val key: String) {
            Transfer("transfer"),
            Swap("swap"),
            Stake("stake"),
            TonConnect("ton_connect")
        }

        enum class RedOperationsOperation(val key: String) {
            Emulate("emulate"),
            Send("send"),
            Quote("quote"),
            Stake("stake"),
            Unstake("unstake"),
            ConnectWallet("connect_wallet"),
            ConfirmTransaction("confirm_transaction")
        }

        enum class RedOperationsOutcome(val key: String) {
            Success("success"),
            Fail("fail"),
            Cancel("cancel")
        }
    }


    interface SendNative {

        enum class SendNativeFeeAsset(val key: String) {
            Coin("coin"),
            BatteryCharges("battery_charges"),
            BatteryTonInstantFee("battery_ton_instant_fee"),
            Gasless("gasless")
        }

        enum class SendNativeFrom(val key: String) {
            WalletScreen("wallet_screen"),
            JettonScreen("jetton_screen"),
            DeepLink("deep_link"),
            TonconnectLocal("tonconnect_local"),
            TonconnectRemote("tonconnect_remote"),
            QrCode("qr_code"),
            Walletconnect("walletconnect")
        }
    }


    interface TradeUiFlow {

        enum class TradeStartedFrom(val key: String) {
            WalletScreen("wallet_screen"),
            JettonScreen("jetton_screen"),
            TabBar("tab_bar"),
            DeepLink("deep_link"),
            QrCode("qr_code")
        }
    }


    interface TransactionSent {

        enum class TransactionSentCategoryDetail(val key: String) {
            Coin("coin"),
            Token("token"),
            Nft("nft"),
            Onchain("onchain"),
            CrossChain("cross_chain"),
            DomainRenew("domain_renew"),
            Subscription("subscription"),
            Multisig("multisig"),
            Deploy("deploy"),
            Unknown("unknown"),
            Stake("stake"),
            Unstake("unstake"),
            Claim("claim"),
            Restake("restake"),
            Compound("compound")
        }
    }


    interface WalletFlow {

        enum class WalletFlowFrom(val key: String) {
            Onboarding("onboarding"),
            Main("main")
        }

        enum class WalletFlowSource(val key: String) {
            Onboarding("onboarding"),
            WalletSetupSection("wallet_setup_section"),
            Settings("settings"),
            DeepLink("deep_link")
        }
    }


    interface WalletOpen {

        enum class WalletOpenWalletInterface(val key: String) {
            V1R1("v1R1"),
            V1R2("v1R2"),
            V1R3("v1R3"),
            V2R1("v2R1"),
            V2R2("v2R2"),
            V3R1("v3R1"),
            V3R2("v3R2"),
            V4R1("v4R1"),
            V4R2("v4R2"),
            V5Beta("v5Beta"),
            V5R1("v5R1"),
            WalletTg("walletTg"),
            Legacy("legacy"),
            P2sh("p2sh"),
            Segwit("segwit"),
            Taproot("taproot"),
            Eoa("eoa"),
            SmartAccount("smart_account")
        }

        enum class WalletOpenWalletMode(val key: String) {
            Multi("multi"),
            Single("single")
        }

        enum class WalletOpenWalletSource(val key: String) {
            Mnemonic("mnemonic"),
            Ledger("ledger"),
            Signer("signer"),
            Keystone("keystone"),
            Privatekey("privatekey"),
            Watchonly("watchonly")
        }
    }


    interface WithdrawFlow {

        enum class WithdrawFlowFrom(val key: String) {
            WalletScreen("wallet_screen"),
            JettonScreen("jetton_screen"),
            DeepLink("deep_link"),
            QrCode("qr_code"),
            HistoryScreen("history_screen"),
            Browser("browser"),
            Collectibles("collectibles"),
            Perps("perps"),
            Walletconnect("walletconnect"),
            Tonconnect("tonconnect"),
            Staking("staking"),
            Migration("migration"),
            Other("other")
        }

        enum class WithdrawFlowWithdrawOption(val key: String) {
            SendTokens("send_tokens"),
            SellToCard("sell_to_card"),
            GetUsdtOtherNetworks("get_usdt_other_networks")
        }

        enum class WithdrawFlowFeeAsset(val key: String) {
            Coin("coin"),
            BatteryCharges("battery_charges"),
            BatteryTonInstantFee("battery_ton_instant_fee"),
            Gasless("gasless")
        }
    }
}
