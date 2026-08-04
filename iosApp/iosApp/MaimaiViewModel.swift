//
//  MaimaiViewModel.swift
//  iosApp
//
//  Created by Mohamed Zidani on 04/08/2026.
//

import Foundation
import shared

class MaiteaViewModel: ObservableObject {
    private let repository: MaiteaRepository = KoinProxy.shared.getMaiteaRepository()
    
    @Published var plays: [MaiteaApiData] = []
    @Published var playerData: MaiteaPlayerDetailsResponse? = nil
    @Published var isLoading: Bool = false
    @Published var currentPage: Int32 = 1
    
    func fetchScores(page: Int32) {
        self.isLoading = true
        repository.getMaiTeaPaginatedData(page: page).collect(collector: Collector<MaiteaPlaysResponse?> { response in
            DispatchQueue.main.async {
                self.plays = response?.data ?? []
                self.isLoading = false
            }
        }) { _ in }
    }
    
    func fetchPlayer() {
        repository.getMaiTeaPlayerDetails().collect(collector: Collector<MaiteaPlayerDetailsResponse?> { response in
            DispatchQueue.main.async {
                self.playerData = response
            }
        }) { _ in }
    }
}

// Utilitaire pour collecter les Flows Kotlin
class Collector<T>: Kotlinx_coroutines_coreFlowCollector {
    let callback: (T) -> Void
    init(callback: @escaping (T) -> Void) { self.callback = callback }
    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        callback(value as! T)
        completionHandler(nil)
    }
}
