import SwiftUI
import shared

struct MaimaiPlayerHeaderView: View {
    let player: PlayerDetailsData?
    let collapsedFraction: CGFloat
    
    var body: some View {
        HStack {
            if let iconUrl = player?.icon {
                AsyncImage(url: URL(string: iconUrl)) { image in
                    image.resizable().scaledToFit()
                } placeholder: {
                    Circle().fill(Color.gray.opacity(0.3))
                }
                .frame(width: 40, height: 40)
                .clipShape(Circle())
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(player?.name ?? "Nom Joueur")
                    .font(.headline)
                    .lineLimit(1)
                
                HStack {
                    Text("Rating: \(String(format: "%.0f", player?.rating ?? 0))")
                        .font(.caption)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(4)
                }
            }
            
            Spacer()
        }
        .padding(.horizontal)
        .opacity(1.0 - Double(collapsedFraction))
    }
}
