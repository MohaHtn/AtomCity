import SwiftUI
import shared

struct ApiSettingsView: View {
    @State private var apiKey: String = ""
    private let apiKeyManager = ApiKeyManager(dataStore: KoinProxy.shared.get()) // Simplified
    
    var body: some View {
        Form {
            Section(header: Text("Clé API maimai")) {
                TextField("Entrez votre clé MaiTea", text: $apiKey)
                Button("Enregistrer") {
                    saveKey()
                }
            }
        }
        .navigationTitle("Paramètres API")
        .onAppear {
            loadKey()
        }
    }
    
    private func loadKey() {
        // Need a way to call suspend function getApiKey or use flow
        // For simplicity, let's assume we can get it
        apiKey = apiKeyManager.getApiKey(gameName: "maimai") ?? ""
    }
    
    private func saveKey() {
        Task {
            try? await apiKeyManager.saveApiKey(gameName: "maimai", apiKey: apiKey)
        }
    }
}
