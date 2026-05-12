package com.kr.eqpro
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.kr.eqpro.audio.EqController
import com.kr.eqpro.databinding.FragmentEqBinding

class EqFragment : Fragment() {
    private var _binding: FragmentEqBinding? = null
    private val binding get() = _binding!!
    private val eqController = EqController()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eqController.init()
        binding.switchDolby.visibility = if (eqController.isDolbySupported()) View.VISIBLE else View.GONE
        binding.switchDolby.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutSpread.visibility = if (isChecked) View.VISIBLE else View.GONE
            eqController.setDolbyMode(isChecked, binding.seekSpread.progress)
        }
        binding.seekSpread.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                binding.txtSpread.text = "Spread: $p%"
                eqController.setDolbyMode(true, p)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eqController.release()
        _binding = null
    }
}
