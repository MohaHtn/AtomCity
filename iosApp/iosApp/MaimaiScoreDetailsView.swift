import SwiftUI
import shared

struct MaimaiScoreDetailsView: View {
    let play: MaiteaApiData
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header with Jacket
                ZStack(alignment: .bottomLeading) {
                    AsyncImage(url: URL(string: play.jacketImageUrl ?? "")) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Rectangle().fill(Color.gray.opacity(0.3))
                    }
                    .frame(height: 250)
                    .clipped()
                    
                    LinearGradient(gradient: Gradient(colors: [.clear, .black.opacity(0.8)]), startPoint: .top, endPoint: .bottom)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(play.song?.name?.jp ?? play.song?.name?.en ?? "")
                            .font(.title).bold().foregroundColor(.white)
                        Text(play.song?.artist?.jp ?? "")
                            .font(.subheadline).foregroundColor(.white.opacity(0.8))
                    }
                    .padding()
                }
                
                // Score Details
                VStack(spacing: 16) {
                    HStack {
                        DetailBadge(title: "Achievement", value: play.achievementFormatted ?? "0.00%", color: .blue)
                        DetailBadge(title: "Rank", value: play.rank ?? "-", color: difficultyColor)
                    }
                    
                    HStack {
                        DetailBadge(title: "Rating", value: String(format: "%.2f", play.rating ?? 0), color: .orange)
                        DetailBadge(title: "Combo", value: play.fullComboLabel ?? "-", color: .green)
                    }
                }
                .padding(.horizontal)
                
                // Extra info
                VStack(alignment: .leading, spacing: 12) {
                    InfoRow(label: "Difficulté", value: play.difficultyLevel?.label ?? "")
                    InfoRow(label: "Date de jeu", value: play.playDate ?? "")
                    InfoRow(label: "ID du score", value: "\(play.id ?? 0)")
                }
                .padding()
                .background(Color(.secondarySystemBackground))
                .cornerRadius(16)
                .padding(.horizontal)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
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

struct DetailBadge: View {
    let title: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack {
            Text(title).font(.caption).foregroundColor(.secondary)
            Text(value).font(.title3).bold().foregroundColor(color)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(color.opacity(0.1))
        .cornerRadius(12)
    }
}

struct InfoRow: View {
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Text(label).foregroundColor(.secondary)
            Spacer()
            Text(value).bold()
        }
    }
}
