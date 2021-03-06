package nl.juraji.biliomi.components.chat.announcements;

import com.google.common.eventbus.Subscribe;
import nl.juraji.biliomi.model.chat.Announcement;
import nl.juraji.biliomi.model.chat.AnnouncementDao;
import nl.juraji.biliomi.model.chat.settings.AnnouncementsSettings;
import nl.juraji.biliomi.model.internal.events.irc.user.messages.IrcChatMessageEvent;
import nl.juraji.biliomi.components.interfaces.TimerService;
import nl.juraji.biliomi.components.system.settings.SettingsService;
import nl.juraji.biliomi.components.shared.ChatService;
import nl.juraji.biliomi.utility.cdi.annotations.qualifiers.BotName;
import nl.juraji.biliomi.utility.events.interceptors.EventBusSubscriber;
import nl.juraji.biliomi.utility.types.Counter;
import nl.juraji.biliomi.utility.types.LoopCounter;
import nl.juraji.biliomi.utility.types.Templater;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Juraji on 21-5-2017.
 * Biliomi v3
 */
@Default
@Singleton
@EventBusSubscriber
public class AnnouncementTimerService extends TimerService {
  private static final String ANNOUNTEMENT_TEMPLATE = "{{message}} ({{id}})";

  @Inject
  private SettingsService settingsService;

  @Inject
  private ChatService chat;

  @Inject
  private AnnouncementDao announcementDao;

  @Inject
  @BotName
  private String botName;

  private final LoopCounter indexCounter = new LoopCounter(0);
  private final Counter messageCounter = new Counter();
  private List<Announcement> announcements;
  private AnnouncementsSettings settings;

  @Override
  public void start() {
    super.start();

    if (settings == null) {
      settings = settingsService.getSettings(AnnouncementsSettings.class, s -> settings = s);
    }

    this.announcements = announcementDao.getList();
    this.indexCounter.setSize(this.announcements.size());

    if (announcements.size() > 0) {
      indexCounter.rand();
      scheduleAtFixedRate(this::runAnnouncements, settings.getRunInterval(), TimeUnit.MILLISECONDS);
    }
  }

  @Subscribe
  public void onIrcChatMessageEvent(IrcChatMessageEvent event) {
    if (!botName.equalsIgnoreCase(event.getUsername())) {
      messageCounter.increment();
    }
  }

  private void runAnnouncements() {
    if (settings.isEnabled() && announcements.size() > 0 && messageCounter.isMoreThan(settings.getMinChatMessages())) {
      Announcement announcement;

      if (settings.isShuffle()) {
        announcement = announcements.get(indexCounter.rand());
      } else {
        announcement = announcements.get(indexCounter.increment());
      }

      chat.say(Templater.template(ANNOUNTEMENT_TEMPLATE)
          .add("message", announcement::getMessage)
          .add("id", announcement::getId));

      messageCounter.reset();
    }
  }
}
