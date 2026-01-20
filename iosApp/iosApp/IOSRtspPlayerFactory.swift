//
//  IOSRtspPlayerFactory.swift
//  iosApp
//
//  Implementation de RtspPlayerFactory pour iOS
//

import Foundation
import UIKit
import AVFoundation
import shared

class IOSRtspPlayerFactory: RtspPlayerFactory {
    static let shared = IOSRtspPlayerFactory()

    func createRtspPlayer(rtspUrl: String, onPlayerReady: (() -> Void)?) -> UIViewController {
        let playerVC = RtspPlayerViewController(rtspUrl: rtspUrl, onPlayerReady: onPlayerReady)
        return playerVC
    }
}

// ViewController pour le lecteur RTSP
class RtspPlayerViewController: UIViewController {
    private let rtspUrl: String
    private var onPlayerReady: (() -> Void)?
    private var player: AVPlayer?
    private var playerLayer: AVPlayerLayer?

    init(rtspUrl: String, onPlayerReady: (() -> Void)?) {
        self.rtspUrl = rtspUrl
        self.onPlayerReady = onPlayerReady
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        setupPlayer()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        playerLayer?.frame = view.bounds
    }

    private func setupPlayer() {
        guard let url = URL(string: rtspUrl) else {
            print("Invalid RTSP URL: \(rtspUrl)")
            showErrorLabel()
            return
        }

        // AVPlayer ne supporte pas directement RTSP
        // On affiche un placeholder pour le moment
        // Pour un vrai support RTSP, il faudrait utiliser une bibliotheque comme VLCKit ou GStreamer

        let label = UILabel()
        label.text = "RTSP Stream\n\(rtspUrl)"
        label.textColor = .white
        label.textAlignment = .center
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(label)

        NSLayoutConstraint.activate([
            label.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            label.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            label.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor, constant: 20),
            label.trailingAnchor.constraint(lessThanOrEqualTo: view.trailingAnchor, constant: -20)
        ])

        // Note: Pour un vrai lecteur RTSP, integrer VLCKit via CocoaPods:
        // pod 'MobileVLCKit'
        // Puis utiliser VLCMediaPlayer pour lire le flux RTSP

        onPlayerReady?()
    }

    private func showErrorLabel() {
        let label = UILabel()
        label.text = "Unable to load stream"
        label.textColor = .red
        label.textAlignment = .center
        label.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(label)

        NSLayoutConstraint.activate([
            label.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            label.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        player?.pause()
        player = nil
    }
}
