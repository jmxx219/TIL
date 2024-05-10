package sample.cafekiosk.spring.api.service.mail;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import sample.cafekiosk.spring.api.client.mail.MailSendClient;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistory;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistoryRepository;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

	// @Spy
	@Mock
	private MailSendClient mailSendClient;

	@Mock
	private MailSendHistoryRepository mailSendHistoryRepository;

	@InjectMocks
	private MailService mailService;

	@DisplayName("메일 전송 테스트")
	@Test
	void sendMail() throws Exception {
	    //given
		// MailSendClient mailSendClient = Mockito.mock(MailSendClient.class);
		// MailSendHistoryRepository mailSendHistoryRepository = Mockito.mock(MailSendHistoryRepository.class);
		// MailService mailService = new MailService(mailSendClient, mailSendHistoryRepository);

		// when(mailSendClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
		// 	.thenReturn(true);

		BDDMockito.given(mailSendClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
			.willReturn(true);

		// spy
		// doReturn(true)
		// 	.when(mailSendClient)
		// 	.sendEmail(anyString(), anyString(), anyString(), anyString());

		//when
		boolean result = mailService.sendMail("", "", "", "");

		//then
	    assertThat(result).isTrue();
		Mockito.verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
	}
}
