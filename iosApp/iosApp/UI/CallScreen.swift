//
//  CallScreen.swift
//  iosApp
//
//  Ecran d'appel complet avec support video et audio
//

import SwiftUI
import linphonesw

/// Ecran principal pour les appels en cours
struct CallScreen: View {

    // MARK: - Properties

    @EnvironmentObject var linphoneManager: LinphoneManager
    let onDismiss: () -> Void

    // MARK: - Body

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if linphoneManager.isIncomingCall {
                IncomingCallOverlay()
            } else if linphoneManager.isCallActive {
                if shouldShowVideoInterface() {
                    VideoCallView()
                } else {
                    AudioCallView()
                }
            } else {
                NoCallView()
            }
        }
        .onAppear {
            print("[CallScreen] Appeared")
            print("[CallScreen]   - isCallActive: \(linphoneManager.isCallActive)")
            print("[CallScreen]   - isIncomingCall: \(linphoneManager.isIncomingCall)")
            print("[CallScreen]   - isVideoEnabled: \(linphoneManager.isVideoEnabled)")
        }
    }

    // MARK: - Private Methods

    private func shouldShowVideoInterface() -> Bool {
        return linphoneManager.isVideoEnabled ||
               linphoneManager.remoteVideoView != nil ||
               linphoneManager.localVideoView != nil
    }

    // MARK: - Subviews

    @ViewBuilder
    private func IncomingCallOverlay() -> some View {
        VStack(spacing: 30) {
            Spacer()

            Text("Appel entrant")
                .font(.title)
                .foregroundColor(.white)

            Text("Via CallKit")
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.7))

            Spacer()
        }
    }

    @ViewBuilder
    private func VideoCallView() -> some View {
        ZStack {
            // Remote video (fullscreen)
            if let remoteView = linphoneManager.remoteVideoView {
                VideoViewRepresentable(videoView: remoteView)
                    .ignoresSafeArea()
            } else {
                Color.black.ignoresSafeArea()
                VStack {
                    Text("Connexion video...")
                        .foregroundColor(.white)
                    Text("En attente du flux distant")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.7))
                }
            }

            // Local video (picture-in-picture)
            VStack {
                HStack {
                    Spacer()
                    if let localView = linphoneManager.localVideoView {
                        VideoViewRepresentable(videoView: localView)
                            .frame(width: 120, height: 160)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.white, lineWidth: 2)
                            )
                            .onTapGesture {
                                linphoneManager.switchCamera()
                            }
                    } else {
                        PlaceholderVideoView()
                    }
                }
                .padding(.top, 60)
                .padding(.trailing, 20)

                Spacer()
            }

            // Call info header
            VStack {
                CallInfoHeader()
                Spacer()
            }

            // Controls footer
            VStack {
                Spacer()
                VideoCallControls()
                    .padding(.bottom, 50)
            }
        }
    }

    @ViewBuilder
    private func AudioCallView() -> some View {
        VStack(spacing: 30) {
            Spacer()

            // Avatar
            Circle()
                .fill(Color.gray.opacity(0.3))
                .frame(width: 120, height: 120)
                .overlay(
                    Image(systemName: "person.fill")
                        .font(.system(size: 50))
                        .foregroundColor(.white)
                )

            // Info
            VStack(spacing: 8) {
                Text(linphoneManager.remoteAddress.isEmpty ? "Appel en cours" : linphoneManager.remoteAddress)
                    .font(.title2)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Text(getCallStatusText())
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.8))
            }

            Spacer()

            // Controls
            AudioCallControls()
                .padding(.bottom, 50)
        }
        .padding()
    }

    @ViewBuilder
    private func NoCallView() -> some View {
        VStack(spacing: 20) {
            Text("Aucun appel actif")
                .foregroundColor(.white)
                .font(.headline)

            Button("Retour") {
                onDismiss()
            }
            .foregroundColor(.blue)
            .padding()
        }
    }

    @ViewBuilder
    private func CallInfoHeader() -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(linphoneManager.remoteAddress.isEmpty ? "Appel" : linphoneManager.remoteAddress)
                    .font(.headline)
                    .foregroundColor(.white)
                    .lineLimit(1)

                Text(getCallStatusText())
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.8))
            }
            Spacer()
        }
        .padding(.top, 60)
        .padding(.horizontal, 20)
    }

    @ViewBuilder
    private func PlaceholderVideoView() -> some View {
        Rectangle()
            .fill(Color.gray.opacity(0.5))
            .frame(width: 120, height: 160)
            .cornerRadius(12)
            .overlay(
                VStack {
                    Image(systemName: "video.slash")
                        .foregroundColor(.white)
                    Text("Camera...")
                        .font(.caption)
                        .foregroundColor(.white)
                }
            )
    }

    @ViewBuilder
    private func VideoCallControls() -> some View {
        HStack(spacing: 40) {
            // End call
            ControlButton(
                icon: "phone.down.fill",
                color: .red,
                action: {
                    linphoneManager.endCall()
                    onDismiss()
                }
            )

            // Switch camera
            ControlButton(
                icon: "camera.rotate",
                color: .gray,
                action: {
                    linphoneManager.switchCamera()
                }
            )

            // Toggle video
            ControlButton(
                icon: linphoneManager.isVideoEnabled ? "video.fill" : "video.slash.fill",
                color: linphoneManager.isVideoEnabled ? .blue : .gray,
                action: {
                    linphoneManager.toggleVideo()
                }
            )
        }
    }

    @ViewBuilder
    private func AudioCallControls() -> some View {
        HStack(spacing: 50) {
            // Microphone
            ControlButton(
                icon: linphoneManager.isMicrophoneEnabled ? "mic.fill" : "mic.slash.fill",
                color: linphoneManager.isMicrophoneEnabled ? .gray : .red,
                action: {
                    linphoneManager.toggleMicrophone()
                }
            )

            // End call
            ControlButton(
                icon: "phone.down.fill",
                color: .red,
                action: {
                    linphoneManager.endCall()
                    onDismiss()
                }
            )

            // Speaker
            ControlButton(
                icon: linphoneManager.isSpeakerEnabled ? "speaker.wave.3.fill" : "speaker.fill",
                color: linphoneManager.isSpeakerEnabled ? .blue : .gray,
                action: {
                    linphoneManager.toggleSpeaker()
                }
            )
        }
    }

    // MARK: - Helpers

    private func getCallStatusText() -> String {
        guard let call = linphoneManager.activeCall ?? linphoneManager.core.currentCall else {
            return linphoneManager.callStatusMessage.isEmpty ? "Aucun appel" : linphoneManager.callStatusMessage
        }

        switch call.state {
        case .OutgoingInit, .OutgoingProgress:
            return "Appel en cours..."
        case .OutgoingRinging:
            return "Ca sonne..."
        case .Connected:
            return "Connecte"
        case .StreamsRunning:
            return "En cours"
        case .IncomingReceived:
            return "Appel entrant"
        case .End, .Released:
            return "Appel termine"
        case .Error:
            return "Erreur"
        default:
            return linphoneManager.callStatusMessage
        }
    }
}

// MARK: - ControlButton

private struct ControlButton: View {
    let icon: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Circle()
                .fill(color)
                .frame(width: 60, height: 60)
                .overlay(
                    Image(systemName: icon)
                        .font(.system(size: 24))
                        .foregroundColor(.white)
                )
        }
    }
}

// MARK: - VideoViewRepresentable

struct VideoViewRepresentable: UIViewRepresentable {
    let videoView: UIView?

    func makeUIView(context: Context) -> UIView {
        let containerView = UIView()
        containerView.backgroundColor = .black

        if let videoView = videoView {
            containerView.addSubview(videoView)
            videoView.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                videoView.topAnchor.constraint(equalTo: containerView.topAnchor),
                videoView.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
                videoView.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
                videoView.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
            ])
        }

        return containerView
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // Update video view if needed
        if let videoView = videoView,
           !uiView.subviews.contains(where: { $0 === videoView }) {
            uiView.subviews.forEach { $0.removeFromSuperview() }

            uiView.addSubview(videoView)
            videoView.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                videoView.topAnchor.constraint(equalTo: uiView.topAnchor),
                videoView.leadingAnchor.constraint(equalTo: uiView.leadingAnchor),
                videoView.trailingAnchor.constraint(equalTo: uiView.trailingAnchor),
                videoView.bottomAnchor.constraint(equalTo: uiView.bottomAnchor)
            ])
        }
    }
}
