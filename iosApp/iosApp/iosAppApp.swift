//
//  iosAppApp.swift
//  iosApp
//
//  Created by Mohamed Zidani on 04/08/2026.
//

import SwiftUI
import shared

@main
struct iosAppApp: App {
    init() {
        KoinIOSKt.doInitKoin(
            scorefetcherApiKey: "rAKno0QrAWd2omdzZDvpEmSQhb4uwoL_j1orIVTUKhz5FQW3tgJpw8I9TTQDpRFqZ7t5yDf6iXhho6QnaYsDCA",
            networkErrorHandler: IosNetworkErrorHandler()
        )
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea(.all, edges: .all)
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
