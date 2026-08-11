//
//  TaikoViewModel.swift
//  iosApp
//
//  Created by Mohamed Zidani on 10/08/2026.
//

import Foundation
import shared
import SwiftUI

class TaikoViewModel: ObservableObject {
    private let useCase: GetTaikoServerDataUseCase = KoinProxy.shared.getTaikoUseCase()
    private let apiKeyManager: ApiKeyManager = KoinProxy.shared.getApiKeyManager()
    
    @Published var scores: [TaikoServerSongHistoryData] = []
    @Published var userSettings: TaikoServerUserSettingsResponse? = nil
    @Published var avatar: UIImage? = nil
    @Published var isLoading: Bool = false
    
    func getScores() {
        guard let userIdString = apiKeyManager.getApiKey(gameName: "taiko"),
              let userId = Int32(userIdString) else {
            print("No valid user ID found for Taiko")
            return
        }
        
        self.isLoading = true
        
        Task {
            // Sequential fetch to match Android logic
            await fetchUserSettings(userId: userId)
            await fetchPlayHistory(userId: userId)
            await getAvatar()
            
            DispatchQueue.main.async {
                self.isLoading = false
            }
        }
    }
    
    private func fetchUserSettings(userId: Int32) async {
        return await withCheckedContinuation { continuation in
            useCase.getUserSettingsFlow(userNumber: String(userId)).collect(collector: Collector<TaikoServerUserSettingsResponse?> { response in
                DispatchQueue.main.async {
                    self.userSettings = response
                    continuation.resume()
                }
            }) { _ in }
        }
    }
    
    private func fetchPlayHistory(userId: Int32) async {
        // We also need music details to merge names
        let musicDetails: TaikoServerMusicDetailsResponse? = await withCheckedContinuation { continuation in
            useCase.getMusicDetailsFlow().collect(collector: Collector<TaikoServerMusicDetailsResponse?> { response in
                continuation.resume(returning: response)
            }) { _ in }
        }
        
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            useCase.getPlayHistoryFlow(userNumber: String(userId)).collect(collector: Collector<TaikoServerPlayHistoryResponse?> { response in
                if let response = response {
                    var merged = response.taikoServerSongHistoryData
                    if let details = musicDetails?.entries {
                        merged = merged.map { score in
                            if let detail = details[String(score.songId)] {
                                score.doCopy(
                                    id: score.id,
                                    songId: score.songId,
                                    difficulty: score.difficulty,
                                    score: score.score,
                                    bad: score.bad,
                                    good: score.good,
                                    great: score.great,
                                    maxCombo: score.maxCombo,
                                    playTime: score.playTime,
                                    drumroll: score.drumroll,
                                    musicName: detail.songName,
                                    musicArtist: detail.artistName
                                )
                            } else {
                                score
                            }
                        }.reversed()
                    }
                    DispatchQueue.main.async {
                        self.scores = merged
                        continuation.resume()
                    }
                } else {
                    continuation.resume()
                }
            }) { _ in }
        }
    }
    
    private func getAvatar() async {
        guard let settings = userSettings else { return }
        
        let kigurumi = String(format: "%04d", settings.kigurumi?.int32Value ?? 0)
        let head = String(format: "%04d", settings.head?.int32Value ?? 0)
        let body = String(format: "%04d", settings.body?.int32Value ?? 0)
        let face = String(format: "%04d", settings.face?.int32Value ?? 0)
        let puchi = String(format: "%04d", settings.puchi?.int32Value ?? 0)
        
        let urls = [
            "https://taiko.farewell.dev/images/Costumes/body/body-\(body).webp",
            "https://taiko.farewell.dev/images/Costumes/head/head-\(head).webp",
            "https://taiko.farewell.dev/images/Costumes/face/face-\(face).webp",
            "https://taiko.farewell.dev/images/Costumes/puchi/puchi-\(puchi).webp"
        ]
        
        let images = await withTaskGroup(of: UIImage?.self) { group in
            for urlString in urls {
                group.addTask {
                    guard let url = URL(string: urlString),
                          let data = try? Data(contentsOf: url) else { return nil }
                    return UIImage(data: data)
                }
            }
            
            var collected: [UIImage] = []
            for await image in group {
                if let image = image {
                    collected.append(image)
                }
            }
            return collected
        }
        
        if !images.isEmpty {
            let size = images[0].size
            let renderer = UIGraphicsImageRenderer(size: size)
            let merged = renderer.image { _ in
                for image in images {
                    image.draw(in: CGRect(origin: .zero, size: size))
                }
            }
            DispatchQueue.main.async {
                self.avatar = merged
            }
        }
    }
}
