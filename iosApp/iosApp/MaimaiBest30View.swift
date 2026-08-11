import SwiftUI
import shared

struct MaimaiBest30View: View {
    @StateObject private var viewModel = MaiteaViewModel()
    
    var body: some View {
        List(viewModel.best30, id: \.id) { entry in
            HStack {
                VStack(alignment: .leading) {
                    Text(entry.song?.name?.jp ?? entry.song?.name?.en ?? "").font(.headline)
                    Text(entry.difficultyLevel?.label ?? "").font(.caption)
                }
                
                Spacer()
                
                VStack(alignment: .trailing) {
                    Text(entry.achievementFormatted ?? "").bold()
                    Text(String(format: "Rating: %.2f", entry.rating ?? 0)).font(.caption)
                }
            }
        }
        .navigationTitle("Best 30")
        .onAppear {
            viewModel.fetchBest30()
        }
    }
}
