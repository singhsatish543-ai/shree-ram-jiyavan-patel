package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.FeePayment
import com.example.data.model.PaymentStatus
import com.example.i18n.AppStrings
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningBg
import com.example.ui.theme.AzureBlue600
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.EmeraldPaidBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoseDue
import com.example.ui.theme.RoseDueBg
import com.example.ui.theme.RoyalBlue700
import com.example.ui.theme.RoyalBlue800
import com.example.ui.theme.RoyalBlue900
import com.example.ui.theme.SkyBlue50
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.util.FeeExportUtils

@Composable
fun ReceiptPreviewDialog(
  payment: FeePayment,
  strings: AppStrings,
  canRefundOrCorrect: Boolean = false,
  onRefundOrCorrect: ((PaymentStatus, String) -> Unit)? = null,
  onShowMessage: (String) -> Unit = {},
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var showRefundDialog by remember { mutableStateOf(false) }

  val status = PaymentStatus.fromRaw(payment.paymentStatus)
  val paidNow = if (payment.amountPaid > 0L) payment.amountPaid else payment.totalAmount
  val (statusBg, statusColor, statusLabel) = when (status) {
    PaymentStatus.PAID -> Triple(EmeraldPaidBg, EmeraldPaid, strings.paidBadge)
    PaymentStatus.PARTIAL -> Triple(AmberWarningBg, AmberWarning, strings.partialBadge)
    PaymentStatus.REFUNDED -> Triple(RoseDueBg, RoseDue, strings.refundedBadge)
    PaymentStatus.CORRECTED -> Triple(RoseDueBg, RoseDue, strings.correctedBadge)
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = PureWhite,
      shadowElevation = 10.dp,
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 560.dp)
        .testTag("receipt_preview_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(RoyalBlue900, AzureBlue600)))
            .padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = PureWhite,
              modifier = Modifier.size(44.dp)
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_school_logo),
                contentDescription = strings.schoolName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.padding(3.dp).clip(CircleShape)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "SHREE RAM JIYAVAN PATEL JUNIOR HIGH SCHOOL",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PureWhite
              )
              Text(
                text = "BECHU KA PURWA, AIRAYAN, KHAGA, FATEHPUR",
                style = MaterialTheme.typography.labelSmall,
                color = PureWhite.copy(alpha = 0.9f)
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Filled.Close, contentDescription = strings.close, tint = PureWhite)
            }
          }
        }

        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = strings.receiptTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = RoyalBlue900
            )
            Surface(shape = RoundedCornerShape(50), color = statusBg) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = statusLabel,
                  tint = statusColor,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = statusLabel,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = statusColor
                )
              }
            }
          }

          HorizontalDivider(color = Slate200)

          ReceiptFieldRow(strings.receiptNoLabel, payment.receiptNo)
          ReceiptFieldRow(strings.dateLabel, payment.paidOnDate)
          ReceiptFieldRow(strings.fullName, payment.studentName)
          ReceiptFieldRow(strings.admissionNo, payment.admissionNo)
          ReceiptFieldRow(strings.fatherName, payment.fatherName.ifBlank { "—" })
          ReceiptFieldRow(strings.classLabel, payment.className)
          ReceiptFieldRow(strings.monthLabel, "${payment.monthName} (${payment.academicYear})")

          HorizontalDivider(color = Slate200)

          ReceiptFieldRow(strings.tuitionFee, formatInr(payment.tuitionFee))
          if (payment.otherFee > 0L) ReceiptFieldRow(strings.otherFee, formatInr(payment.otherFee))
          if (payment.lateFee > 0L) ReceiptFieldRow(strings.lateFee, formatInr(payment.lateFee))
          if (payment.discount > 0L) ReceiptFieldRow(strings.discount, "-${formatInr(payment.discount)}")
          ReceiptFieldRow(strings.totalAmountLabel, formatInr(payment.totalAmount))
          ReceiptFieldRow(strings.previousBalanceLabel, formatInr(payment.previousBalance))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = SkyBlue50,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = strings.amountPaidLabel,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue900
                )
                Text(
                  text = formatInr(paidNow),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue800
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = strings.remainingBalanceLabel,
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate700
                )
                Text(
                  text = formatInr(payment.remainingBalance),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (payment.remainingBalance > 0L) RoseDue else EmeraldPaid
                )
              }
            }
          }

          ReceiptFieldRow(strings.paymentMode, paymentModeLabel(payment.paymentMode, strings))
          if (payment.remarks.isNotBlank()) {
            ReceiptFieldRow(strings.remarksHint, payment.remarks)
          }
          if (payment.refundOrCorrectionReason.isNotBlank()) {
            Surface(shape = RoundedCornerShape(10.dp), color = RoseDueBg, modifier = Modifier.fillMaxWidth()) {
              Text(
                text = "${statusLabel}: ${payment.refundOrCorrectionReason}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = RoseDue,
                modifier = Modifier.padding(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Authorized Signature strip
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Column {
              Text(
                text = strings.collectedByLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
              Text(
                text = payment.collectedBy.ifBlank { "School Admin" },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Slate700
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "______________________",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
              Text(
                text = strings.authorizedSignatureLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = RoyalBlue900
              )
            }
          }

          HorizontalDivider(color = Slate200)

          // Receipt Export Actions: Download PDF, Print Receipt, Share Receipt
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = {
                val file = FeeExportUtils.saveReceiptPdfToDownloads(context, payment, strings)
                onShowMessage("${strings.receiptSavedMsg}: ${file.name}")
              },
              border = BorderStroke(1.dp, RoyalBlue700),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("download_receipt_pdf_button")
            ) {
              Icon(Icons.Filled.Download, contentDescription = strings.downloadPdfLabel, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(strings.exportPdf, color = RoyalBlue800, style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
              onClick = { FeeExportUtils.printFeeReceipt(context, payment, strings) },
              border = BorderStroke(1.dp, RoyalBlue700),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("print_receipt_button")
            ) {
              Icon(Icons.Filled.Print, contentDescription = strings.printReceiptLabel, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(strings.printReceiptLabel, color = RoyalBlue800, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }

            OutlinedButton(
              onClick = {
                runCatching { FeeExportUtils.shareReceiptPdf(context, payment, strings) }
                  .onFailure { onShowMessage(it.localizedMessage ?: "Share unavailable") }
              },
              border = BorderStroke(1.dp, RoyalBlue700),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("share_receipt_button")
            ) {
              Icon(Icons.Outlined.Share, contentDescription = strings.shareReceipt, tint = RoyalBlue800, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(strings.shareReceipt, color = RoyalBlue800, style = MaterialTheme.typography.labelMedium, maxLines = 1)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (canRefundOrCorrect && !payment.isVoided() && onRefundOrCorrect != null) {
              TextButton(
                onClick = { showRefundDialog = true },
                modifier = Modifier
                  .weight(1f)
                  .testTag("refund_correction_button")
              ) {
                Icon(Icons.Outlined.HistoryEdu, contentDescription = strings.refundCorrectionBtn, tint = RoseDue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.refundCorrectionBtn, color = RoseDue, style = MaterialTheme.typography.labelMedium)
              }
            }
            Button(
              onClick = onDismiss,
              colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue800),
              modifier = Modifier
                .weight(1f)
                .testTag("close_receipt_button")
            ) {
              Text(strings.close, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  if (showRefundDialog && onRefundOrCorrect != null) {
    RefundOrCorrectionDialog(
      receiptNo = payment.receiptNo,
      strings = strings,
      onConfirm = { newStatus, reason ->
        onRefundOrCorrect(newStatus, reason)
        showRefundDialog = false
      },
      onDismiss = { showRefundDialog = false }
    )
  }
}

@Composable
private fun ReceiptFieldRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = Slate500,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = Slate700
    )
  }
}

@Composable
private fun RefundOrCorrectionDialog(
  receiptNo: String,
  strings: AppStrings,
  onConfirm: (PaymentStatus, String) -> Unit,
  onDismiss: () -> Unit
) {
  var reason by remember { mutableStateOf("") }
  val validReason = reason.trim().length >= 3

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("${strings.refundCorrectionTitle} ($receiptNo)", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(strings.deleteReceiptConfirm, style = MaterialTheme.typography.bodySmall, color = Slate700)
        OutlinedTextField(
          value = reason,
          onValueChange = { reason = it.take(200) },
          label = { Text(strings.refundReasonHint) },
          singleLine = false,
          maxLines = 3,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("refund_reason_input")
        )
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
          onClick = { onConfirm(PaymentStatus.CORRECTED, reason.trim()) },
          enabled = validReason,
          modifier = Modifier.testTag("confirm_mark_corrected_button")
        ) {
          Text(strings.markCorrectedBtn, color = RoyalBlue800)
        }
        Button(
          onClick = { onConfirm(PaymentStatus.REFUNDED, reason.trim()) },
          enabled = validReason,
          colors = ButtonDefaults.buttonColors(containerColor = RoseDue),
          modifier = Modifier.testTag("confirm_mark_refunded_button")
        ) {
          Text(strings.markRefundedBtn)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel, color = Slate500)
      }
    }
  )
}
