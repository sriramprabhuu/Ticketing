package com.reserve.presentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.reserve.exception.TicketingException;
import com.reserve.service.RefService;
import com.reserve.service.TicketService;
import com.reserve.service.UserService;
import com.reserve.utilities.DateUtils;
import com.reserve.vo.LevelMaster;
import com.reserve.vo.RegisterVO;
import com.reserve.vo.Reservation;
import com.reserve.vo.SeatHold;
import com.reserve.vo.User;

@Controller
public class TicketController {

	final static Logger logger = Logger.getLogger(TicketController.class);

	@Autowired
	private RefService refService;

	@Autowired
	private UserService userService;

	@Autowired
	private TicketService ticketService;

	@RequestMapping(value = "/loadHome")
	public ModelAndView list(Model model, HttpSession session) {
		logger.debug("Open home page");
		ModelAndView modelView = new ModelAndView("loadLevels");
		List<LevelMaster> list = null;
		Map<Integer, String> levels = null;
		try {
			if (session.getAttribute("levelMasterList") == null) {
				list = refService.getLevels();
				levels = new HashMap<Integer, String>();
				for (LevelMaster levelMaster : list) {
					levels.put(levelMaster.getLevelId(), levelMaster.getLevelName());
				}
				session.setAttribute("levelMasterList", levels);
				// modelView.addObject("levelList", levels);
			}
		} catch (TicketingException e) {
			logger.fatal(e.getMessage());
		} catch (Exception e) {
			logger.fatal(e.getMessage());
		}
		logger.debug("Loading home page");
		model.addAttribute("registerVO", new RegisterVO());
		return modelView;
	}

	@RequestMapping(value = "/findSeats")
	public ModelAndView findSeats(Model model, RegisterVO id, BindingResult bindingResult) {
		LevelValidator validator = new LevelValidator();
		validator.validate(id, bindingResult);
		int numberOfSeats = 0;
		try {
			if (!bindingResult.hasErrors()) {
				numberOfSeats = ticketService.numSeatsAvailable(Optional.of(id.getSelectedLevelId()));
				model.addAttribute("message", "There are " + numberOfSeats + " seats available.");
			}
		} catch (TicketingException e) {
			model.addAttribute("message", e.getErrorMessage());
			logger.fatal(e.getMessage());
		} catch (Exception e) {
			model.addAttribute("message", e.getMessage());
			logger.fatal(e.getMessage());
		}
		ModelAndView modelView = new ModelAndView("loadLevels");
		logger.debug("Loading seats");
		return modelView;
	}

	@RequestMapping(value = "/holdSeats")
	public ModelAndView holdSeats(Model model, RegisterVO id, BindingResult bindingResult, HttpSession session) {
		logger.debug("Holding seats");
		LevelValidator validator = new LevelValidator();
		validator.validateBeforeHolding(id, bindingResult);
		ModelAndView modelView = null;
		SeatHold hold = null;
		User user = new User();
		try {
			if (bindingResult.hasErrors()) {
				modelView = new ModelAndView("loadLevels");
				id.setFlagShow(1);
			} else {
				user.setEmail(id.getEmail().trim());
				user = userService.getUserOrSave(user);
				logger.debug("User Saved");
				session.setAttribute("userLogged", user);
				hold = ticketService.findAndHoldSeats(id.getNoOfSeats(), Optional.of(id.getMinLevel()),
						Optional.of(id.getMaxLevel()), id.getEmail().trim());
				logger.debug("Seats Held");
				if (hold != null) {
					id.setHoldId(hold.getHoldId());
				}
				logger.info("Tickets Held, Hold number - " + hold.getHoldId() + ", " + hold.toString());
				model.addAttribute("message",
						"Tickets Held<BR> Hold number / ID - " + hold.getHoldId() + ", " + hold.toString()
								+ "<BR> Tickets will be held until "
								+ DateUtils.getValidUntilDate(hold.getCreatedDate()));
				model.addAttribute("registerVO", id);
				modelView = new ModelAndView("confirmTickets");
			}
		} catch (TicketingException e) {
			modelView = new ModelAndView("loadLevels");
			id.setFlagShow(1);
			model.addAttribute("errorMessage", e.getErrorMessage());
			logger.fatal(e.getMessage());
		} catch (Exception e) {
			modelView = new ModelAndView("loadLevels");
			id.setFlagShow(1);
			model.addAttribute("errorMessage", e.getMessage());
			logger.fatal(e.getMessage());
		}
		logger.debug("Loading hold information");
		return modelView;
	}

	@RequestMapping(value = "/confirmSeats")
	public ModelAndView confirmSeats(Model model, RegisterVO id, BindingResult bindingResult, HttpSession session) {
		LevelValidator validator = new LevelValidator();
		validator.validateBeforeConfirm(id, bindingResult);
		ModelAndView modelView = null;
		Reservation reservation = null;
		try {
			if (!bindingResult.hasErrors()) {
				reservation = ticketService.reserveSeats(id.getHoldId(), id.getEmail());
				logger.info("Reservation done " + reservation.toString());
				model.addAttribute("message", reservation.toString());
			}
		} catch (TicketingException e) {
			model.addAttribute("message", e.getErrorMessage());
			logger.fatal(e.getMessage());
		} catch (Exception e) {
			model.addAttribute("message", e.getMessage());
			logger.fatal(e.getMessage());
		}
		model.addAttribute("registerVO", id);
		modelView = new ModelAndView("confirmTickets");
		logger.debug("Loading booking information");
		return modelView;
	}

	@RequestMapping(value = "/goToConfirm")
	public ModelAndView goToConfirm(Model model, HttpSession session) {
		model.addAttribute("registerVO", new RegisterVO());
		ModelAndView modelView = new ModelAndView("confirmTickets");
		logger.debug("Loading booking information");
		return modelView;
	}
}