package com.idsolution.icondoapp.feature.ssh.presenter.sites

import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.idsolution.icondoapp.core.presentation.helper.Loading
import com.idsolution.icondoapp.core.presentation.helper.Resource

data class CondoSitesState(
    val sites: Resource<List<CondoSite>> = Loading(),
)