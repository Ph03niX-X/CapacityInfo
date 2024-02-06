package com.ph03nix_x.capacityinfo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.databinding.LastChargeNoPremiumFragmentBinding
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface

/**
 * Created by Ph03niX-X on 06.02.2024
 * Ph03niX-X@outlook.com
 */
class LastChargeNoPremiumFragment : Fragment(R.layout.last_charge_no_premium_fragment),
    NavigationInterface {
    private lateinit var binding: LastChargeNoPremiumFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = LastChargeNoPremiumFragmentBinding.inflate(inflater, container, false)
        return binding.root.rootView
    }

    override fun onResume() {
        if(PremiumInterface.isPremium) {
            MainActivity.instance?.loadFragment(LastChargeFragment())
            MainActivity.instance?.fragment = LastChargeFragment()
        }
        super.onResume()
    }
}