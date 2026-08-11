//
//  AtomCityUsersView.swift
//  iosApp
//
//  Created by Mohamed Zidani on 10/08/2026.
//

import SwiftUI
import shared

struct AtomCityUsersView: View {
    @StateObject private var viewModel = MaiteaViewModel()
    
    var body: some View {
        List {
            Section(footer: Text("Les utilisateurs listés ici se sont enregistrés sur l'application. Ils peuvent être utilisés pour les fonctionnalités de partage de données ou de classement.")) {
                if viewModel.isLoadingProfiles {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                } else {
                    ForEach(Array(viewModel.profiles.keys).sorted(), id: \.self) { key in
                        if let username = viewModel.profiles[key] {
                            VStack(alignment: .leading) {
                                Text(username)
                                    .font(.headline)
                            }
                            .padding(.vertical, 4)
                        }
                    }
                }
            }
        }
        .navigationTitle("Utilisateurs")
        .onAppear {
            viewModel.fetchProfiles()
        }
    }
}
