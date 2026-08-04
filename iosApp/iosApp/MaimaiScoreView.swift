//
//  MaimaiScoreView.swift
//  iosApp
//
//  Created by Mohamed Zidani on 04/08/2026.
//

import SwiftUI
import shared

struct MaimaiScoresView: View {
    @StateObject private var viewModel = MaiteaViewModel()
    
    var body: some View {
        NavigationView {
            List(viewModel.plays, id: \.id) { play in
                MaimaiScoreItemView(play: play)
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
            }
            .listStyle(.plain)
            .navigationTitle("maimai Scores")
            .onAppear {
                viewModel.fetchScores(page: 1)
                viewModel.fetchPlayer()
            }
            .overlay {
                if viewModel.isLoading { ProgressView() }
            }
        }
    }
}
