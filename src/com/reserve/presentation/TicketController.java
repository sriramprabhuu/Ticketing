package com.reserve.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.reserve.service.RefService;
import com.reserve.service.TicketService;
import com.reserve.service.UserService;
import com.reserve.vo.LevelMaster;
import com.reserve.vo.RegisterVO;
import com.reserve.vo.SeatHold;
import com.reserve.vo.User;

@Controller
public class TicketController {

	@Autowired
	private RefService refService;

	@Autowired
	private UserService userService;

	@Autowired
	private TicketService ticketService;

	@RequestMapping(value = "/loadHome")
	public ModelAndView list(Model model, HttpSession session) {
		ModelAndView modelView = new ModelAndView("loadLevels");
		List<LevelMaster> list = null;
		Map<Integer, String> levels = null;
		if (session.getAttribute("levelMasterList") == null) {
			list = refService.getLevels();
			levels = new HashMap<Integer, String>();
			for (LevelMaster levelMaster : list) {
				levels.put(levelMaster.getLevelId(), levelMaster.getLevelName());
			}
			session.setAttribute("levelMasterList", levels);
			// modelView.addObject("levelList", levels);
		}
		model.addAttribute("registerVO", new RegisterVO());
		return modelView;
	}

	@RequestMapping(value = "/findSeats")
	public ModelAndView findSeats(Model model, RegisterVO id,
			BindingResult bindingResult) {
		LevelValidator validator = new LevelValidator();
		validator.validate(id, bindingResult);
		int numberOfSeats = 0;
		if (!bindingResult.hasErrors()) {
			numberOfSeats = ticketService.numSeatsAvailable(Optional.of(id
					.getSelectedLevelId()));
			model.addAttribute("message", "There are " + numberOfSeats
					+ " seats available.");
		}
		ModelAndView modelView = new ModelAndView("loadLevels");
		return modelView;
	}

	@RequestMapping(value = "/holdSeats")
	public ModelAndView holdSeats(Model model, RegisterVO id,
			BindingResult bindingResult, HttpSession session) {
		LevelValidator validator = new LevelValidator();
		validator.validateBeforeHolding(id, bindingResult);
		ModelAndView modelView = null;
		SeatHold hold = null;
		User user = new User();
		if (bindingResult.hasErrors()) {
			modelView = new ModelAndView("loadLevels");
			id.setFlagShow(1);
		} else {
			user.setEmail(id.getEmail().trim());
			user = userService.getUserOrSave(user);
			session.setAttribute("userLogged", user);
			hold = ticketService.findAndHoldSeats(id.getNoOfSeats(),
					Optional.of(id.getMinLevel()),
					Optional.of(id.getMaxLevel()), id.getEmail().trim());
			if (hold != null) {
				id.setHoldId(hold.getHoldId());
			}
			modelView = new ModelAndView("confirmTickets");
		}
		model.addAttribute("registerVO", id);
		return modelView;
	}

	@RequestMapping(value = "/confirmSeats")
	public ModelAndView confirmSeats(Model model, RegisterVO id,
			BindingResult bindingResult, HttpSession session) {
		LevelValidator validator = new LevelValidator();
		validator.validateBeforeHolding(id, bindingResult);
		User user = (User) session.getAttribute("userLogged");
		ModelAndView modelView = null;
		if (!bindingResult.hasErrors()) {
			ticketService.reserveSeats(id.getHoldId(), user.getEmail());
		}
		model.addAttribute("registerVO", id);
		modelView = new ModelAndView("confirmTickets");
		return modelView;
	}
}