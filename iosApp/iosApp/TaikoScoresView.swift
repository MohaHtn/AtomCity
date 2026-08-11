//
//  TaikoScoresView.swift
//  iosApp
//
//  Created by Mohamed Zidani on 10/08/2026.
//

import SwiftUI
import shared

struct TaikoScoresView: View {
    @StateObject private var viewModel = TaikoViewModel()
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                if let settings = viewModel.userSettings {
                    playerHeader(settings: settings)
                }
                
                if viewModel.isLoading && viewModel.scores.isEmpty {
                    ProgressView()
                } else {
                    ForEach(viewModel.scores, id: \.self) { score in
                        TaikoScoreCard(score: score)
                    }
                }
            }
            .padding()
        }
        .navigationTitle("Taiko no Tatsujin")
        .onAppear {
            viewModel.getScores()
        }
    }
    
    @ViewBuilder
    private func playerHeader(settings: TaikoServerUserSettingsResponse) -> some View {
        HStack(spacing: 16) {
            if let avatar = viewModel.avatar {
                Image(uiImage: avatar)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 80, height: 80)
            }
            
            VStack(alignment: .leading) {
                Text(settings.myDonName ?? "Chargement...")
                    .font(.title2).bold()
                Text(settings.title ?? "")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
    }
}

struct TaikoScoreCard: View {
    let score: TaikoServerSongHistoryData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(score.musicName ?? "Inconnu")
                    .font(.headline)
                    .foregroundColor(.white)
                Spacer()
                Text(difficultyName(score.difficulty?.int32Value))
                    .font(.caption).bold()
                    .foregroundColor(.white.opacity(0.8))
            }
            
            HStack(alignment: .bottom) {
                VStack(alignment: .leading) {
                    Text(score.musicArtist ?? "")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.7))
                    Text(formatDate(score.playTime))
                        .font(.caption2)
                        .foregroundColor(.white.opacity(0.6))
                }
                
                Spacer()
                
                Text("\(score.score?.int32Value ?? 0)")
                    .font(.title).bold()
                    .foregroundColor(.white)
            }
        }
        .padding()
        .background(difficultyColor(score.difficulty?.int32Value))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
    
    func difficultyName(_ diff: Int32?) -> String {
        switch diff {
        case 1: return "Kantan"
        case 2: return "Futsuu"
        case 3: return "Muzukashii"
        case 4: return "Oni"
        case 5: return "Ura Oni"
        default: return "Inconnu"
        }
    }
    
    func difficultyColor(_ diff: Int32?) -> Color {
        switch diff {
        case 1: return Color(red: 0xCF/255.0, green: 0x2C/255.0, blue: 0x00/255.0)
        case 2: return Color(red: 0x65/255.0, green: 0x7E/255.0, blue: 0x25/255.0)
        case 3: return Color(red: 0x22/255.0, green: 0x30/255.0, blue: 0x04/255.0)
        case 4: return Color(red: 0xCE/255.0, green: 0x2D/255.0, blue: 0x76/255.0)
        case 5: return Color(red: 0x6B/255.0, green: 0x1D/255.0, blue: 0x8C/255.0)
        default: return .gray
        }
    }
    
    func formatDate(_ dateString: String?) -> String {
        // Simple formatting for now, similar to formatPlayDate in Android if available
        return dateString ?? ""
    }
}
