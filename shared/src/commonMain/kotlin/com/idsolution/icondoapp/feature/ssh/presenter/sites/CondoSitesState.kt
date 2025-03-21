package com.example.testkmpapp.feature.ssh.presenter.sites

import com.example.testkmpapp.feature.ssh.domain.models.CondoSite

data class CondoSitesState(
    val sites: List<CondoSite> = emptyList(),
)