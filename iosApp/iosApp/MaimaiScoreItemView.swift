//
//  MaimaiScoreItemView.swift
//  iosApp
//
//  Created by Mohamed Zidani on 04/08/2026.
//
import SwiftUI
import shared

struct MaimaiScoreItemView: View {
    let play: MaiteaApiData
    
    var body: some View {
        HStack(spacing: 12) {
            // Jacket Image Placeholder
            AsyncImage(url: URL(string: play.jacketImageUrl ?? "")) { image in
                image.resizable().scaledToFill()
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(width: 60, height: 60)
            .clipShape(Circle())
            .overlay(Circle().stroke(difficultyColor, lineWidth: 2))
            
            VStack(alignment: .leading) {
                Text(play.song?.name?.jp ?? play.song?.name?.en ?? "Unknown")
                    .font(.headline).bold()
                Text(play.song?.artist?.jp ?? "")
                    .font(.caption).foregroundColor(.secondary)
                
                Text(play.difficultyLevel?.label?.uppercased() ?? "")
                    .font(.system(size: 10, weight: .black))
                    .padding(.horizontal, 6).padding(.vertical, 2)
                    .background(difficultyColor)
                    .foregroundColor(.white)
                    .cornerRadius(4)
            }
            
            Spacer()
            
            VStack(alignment: .trailing) {
                Text(play.rank ?? "").font(.title2).bold().foregroundColor(difficultyColor)
                Text(play.achievementFormatted ?? "0.00%").font(.body).bold()
            }
        }
        .padding(12)
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(radius: 2)
    }
    
    var difficultyColor: Color {
        switch play.difficultyLevel?.value?.lowercased() {
        case "easy": return .blue
        case "basic": return .green
        case "advanced": return .yellow
        case "expert": return .red
        case "master": return .purple
        case "remaster": return .pink
        default: return .gray
        }
    }
}
