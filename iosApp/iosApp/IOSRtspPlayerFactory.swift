//
//  IOSRtspPlayerFactory.swift
//  iosApp
//
//  Implementation de RtspPlayerFactory pour iOS utilisant VLCKit
//

import Foundation
import UIKit
import MobileVLCKit
import shared

class IOSRtspPlayerFactory: RtspPlayerFactory {
    static let shared = IOSRtspPlayerFactory()

    func createRtspPlayer(rtspUrl: String, onPlayerReady: (() -> Void)?) -> UIViewController {
        let playerVC = VLCRtspPlayerViewController(rtspUrl: rtspUrl, onPlayerReady: onPlayerReady)
        return playerVC
    }
}

// MARK: - VLC RTSP Player ViewController

class VLCRtspPlayerViewController: UIViewController {

    // MARK: - Properties

    private let rtspUrl: String
    private var onPlayerReady: (() -> Void)?

    private var mediaPlayer: VLCMediaPlayer?
    private var videoView: UIView!
    private var statusLabel: UILabel!
    private var loadingIndicator: UIActivityIndicatorView!

    private var playerState: String = "INIT" {
        didSet {
            updateStatusLabel()
        }
    }

    // MARK: - Initialization

    init(rtspUrl: String, onPlayerReady: (() -> Void)?) {
        self.rtspUrl = rtspUrl
        self.onPlayerReady = onPlayerReady
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .black
        setupUI()
        setupPlayer()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        videoView.frame = view.bounds
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopPlayer()
    }

    deinit {
        stopPlayer()
    }

    // MARK: - UI Setup

    private func setupUI() {
        // Video view
        videoView = UIView()
        videoView.backgroundColor = .black
        videoView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(videoView)

        NSLayoutConstraint.activate([
            videoView.topAnchor.constraint(equalTo: view.topAnchor),
            videoView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            videoView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            videoView.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])

        // Status label
        statusLabel = UILabel()
        statusLabel.textColor = .white
        statusLabel.font = .systemFont(ofSize: 12, weight: .medium)
        statusLabel.textAlignment = .center
        statusLabel.backgroundColor = UIColor.black.withAlphaComponent(0.7)
        statusLabel.layer.cornerRadius = 4
        statusLabel.clipsToBounds = true
        statusLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(statusLabel)

        NSLayoutConstraint.activate([
            statusLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 8),
            statusLabel.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -8),
            statusLabel.widthAnchor.constraint(greaterThanOrEqualToConstant: 80),
            statusLabel.heightAnchor.constraint(equalToConstant: 28)
        ])

        // Loading indicator
        loadingIndicator = UIActivityIndicatorView(style: .large)
        loadingIndicator.color = .white
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(loadingIndicator)

        NSLayoutConstraint.activate([
            loadingIndicator.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            loadingIndicator.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])

        loadingIndicator.startAnimating()
    }

    private func updateStatusLabel() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }

            self.statusLabel.text = "  \(self.playerState)  "

            switch self.playerState {
            case "PLAYING":
                self.statusLabel.textColor = .green
                self.loadingIndicator.stopAnimating()
            case "BUFFERING":
                self.statusLabel.textColor = .yellow
                self.loadingIndicator.startAnimating()
            case "ERROR":
                self.statusLabel.textColor = .red
                self.loadingIndicator.stopAnimating()
            default:
                self.statusLabel.textColor = .white
            }
        }
    }

    // MARK: - Player Setup

    private func setupPlayer() {
        print("[VLCRtspPlayer] Setting up player for: \(rtspUrl)")

        guard let url = URL(string: rtspUrl) else {
            print("[VLCRtspPlayer] Invalid URL: \(rtspUrl)")
            playerState = "ERROR"
            showError("Invalid RTSP URL")
            return
        }

        // Create VLC media player
        mediaPlayer = VLCMediaPlayer()
        mediaPlayer?.delegate = self
        mediaPlayer?.drawable = videoView

        // Create media with RTSP options
        let media = VLCMedia(url: url)

        // Configure for minimal latency RTSP streaming (security cameras)
        media.addOption(":network-caching=150")      // Reduced from 300ms
        media.addOption(":live-caching=150")         // Live stream caching
        media.addOption(":rtsp-tcp")                 // TCP for reliability
        media.addOption(":no-video-title-show")
        media.addOption(":clock-jitter=0")
        media.addOption(":clock-synchro=0")
        media.addOption(":rtsp-frame-buffer-size=100000")  // Smaller buffer
        media.addOption(":file-caching=0")           // No file caching
        media.addOption(":disc-caching=0")           // No disc caching
        media.addOption(":sout-mux-caching=0")       // No mux caching

        mediaPlayer?.media = media

        // Mute audio by default (security cameras)
        mediaPlayer?.audio?.volume = 0

        // Start playing
        playerState = "CONNECTING"
        mediaPlayer?.play()

        print("[VLCRtspPlayer] Player started")
    }

    private func stopPlayer() {
        print("[VLCRtspPlayer] Stopping player")
        mediaPlayer?.stop()
        mediaPlayer?.delegate = nil
        mediaPlayer = nil
    }

    private func showError(_ message: String) {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }

            let errorLabel = UILabel()
            errorLabel.text = message
            errorLabel.textColor = .red
            errorLabel.textAlignment = .center
            errorLabel.numberOfLines = 0
            errorLabel.translatesAutoresizingMaskIntoConstraints = false
            self.view.addSubview(errorLabel)

            NSLayoutConstraint.activate([
                errorLabel.centerXAnchor.constraint(equalTo: self.view.centerXAnchor),
                errorLabel.centerYAnchor.constraint(equalTo: self.view.centerYAnchor),
                errorLabel.leadingAnchor.constraint(greaterThanOrEqualTo: self.view.leadingAnchor, constant: 20),
                errorLabel.trailingAnchor.constraint(lessThanOrEqualTo: self.view.trailingAnchor, constant: -20)
            ])
        }
    }
}

// MARK: - VLCMediaPlayerDelegate

extension VLCRtspPlayerViewController: VLCMediaPlayerDelegate {

    func mediaPlayerStateChanged(_ aNotification: Notification) {
        guard let player = mediaPlayer else { return }

        let state = player.state

        print("[VLCRtspPlayer] State changed: \(state.rawValue)")

        switch state {
        case .opening:
            playerState = "OPENING"

        case .buffering:
            playerState = "BUFFERING"

        case .playing:
            playerState = "PLAYING"
            onPlayerReady?()
            onPlayerReady = nil

        case .paused:
            playerState = "PAUSED"

        case .stopped:
            playerState = "STOPPED"

        case .ended:
            playerState = "ENDED"

        case .error:
            playerState = "ERROR"
            showError("Playback error\nCheck URL and network")

        case .esAdded:
            print("[VLCRtspPlayer] Elementary stream added")

        @unknown default:
            print("[VLCRtspPlayer] Unknown state: \(state.rawValue)")
        }
    }

    func mediaPlayerTimeChanged(_ aNotification: Notification) {
        // Optional: handle time updates
    }
}
