import SwiftUI

struct MaimaiImportOverlay: View {
    let progress: Int32
    let message: String
    
    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 20) {
                Text("Importation des scores")
                    .font(.headline)
                    .fontWeight(.bold)
                
                ZStack {
                    CircularProgressView(progress: Double(progress) / 100.0)
                        .frame(width: 80, height: 80)
                    
                    Text("\(progress)%")
                        .font(.subheadline)
                        .fontWeight(.bold)
                }
                
                Text(message)
                    .font(.caption)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                
                Text("Cet import est fait en arrière-plan. Veuillez patienter.")
                    .font(.system(size: 10))
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(24)
            .background(Color(.systemBackground))
            .cornerRadius(24)
            .shadow(radius: 10)
            .padding(40)
        }
    }
}

struct CircularProgressView: View {
    let progress: Double
    
    var body: some View {
        ZStack {
            Circle()
                .stroke(lineWidth: 8)
                .opacity(0.3)
                .foregroundColor(.blue)
            
            Circle()
                .trim(from: 0.0, to: CGFloat(min(self.progress, 1.0)))
                .stroke(style: StrokeStyle(lineWidth: 8, lineCap: .round, lineJoin: .round))
                .foregroundColor(.blue)
                .rotationEffect(Angle(degrees: 270.0))
                .animation(.linear, value: progress)
        }
    }
}
