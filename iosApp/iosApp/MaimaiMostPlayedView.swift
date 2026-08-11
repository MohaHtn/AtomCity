import SwiftUI
import shared

struct MaimaiMostPlayedView: View {
    @StateObject private var viewModel = MaiteaViewModel()
    @State private var period = "month"
    
    var body: some View {
        VStack {
            Picker("Période", selection: $period) {
                Text("Jour").tag("day")
                Text("Semaine").tag("week")
                Text("Mois").tag("month")
            }
            .pickerStyle(SegmentedPickerStyle())
            .padding()
            .onChange(of: period) { newValue in
                viewModel.fetchMostPlayed(isGlobal: true, period: newValue)
            }
            
            List(viewModel.mostPlayed, id: \.songName) { entry in
                HStack {
                    AsyncImage(url: URL(string: entry.jacketImageUrl ?? "")) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Rectangle().fill(Color.gray.opacity(0.3))
                    }
                    .frame(width: 50, height: 50)
                    .cornerRadius(8)
                    
                    VStack(alignment: .leading) {
                        Text(entry.songName ?? "Inconnu").font(.headline)
                        Text(entry.difficulty ?? "").font(.caption).foregroundColor(.secondary)
                    }
                    
                    Spacer()
                    
                    Text("\(entry.playCount) fois")
                        .font(.subheadline).bold()
                }
            }
        }
        .navigationTitle("Le plus joué")
        .onAppear {
            viewModel.fetchMostPlayed(isGlobal: true, period: period)
        }
    }
}
