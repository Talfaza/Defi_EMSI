import 'package:flutter/material.dart';
import '../app_colors.dart';
import '../services/blockchain_service.dart';
import '../services/payment_service.dart';

class ClinicShell extends StatefulWidget {
  final String clinicName;
  final String walletAddress;
  final int clinicId;
  
  const ClinicShell({
    super.key,
    required this.clinicName,
    required this.walletAddress,
    required this.clinicId,
  });

  @override
  State<ClinicShell> createState() => _ClinicShellState();
}

class _ClinicShellState extends State<ClinicShell> {
  int _currentIndex = 0;

  @override
  Widget build(BuildContext context) {
    final List<Widget> pages = [
      _ClinicHomePage(
        clinicName: widget.clinicName,
        walletAddress: widget.walletAddress,
        clinicId: widget.clinicId,
      ),
      _ClinicRequestPaymentPage(clinicId: widget.clinicId),
      _ClinicHistoryPage(clinicId: widget.clinicId),
      const _ClinicSettingsPage(),
    ];

    return Scaffold(
      body: pages[_currentIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        selectedItemColor: AppColors.mint,
        unselectedItemColor: Colors.grey[400],
        type: BottomNavigationBarType.fixed,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.home_outlined),
            label: 'Home',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.request_page_outlined),
            label: 'Request',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.history),
            label: 'History',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings_outlined),
            label: 'Settings',
          ),
        ],
      ),
    );
  }
}

class _ClinicHomePage extends StatefulWidget {
  final String clinicName;
  final String walletAddress;
  final int clinicId;
  
  const _ClinicHomePage({
    required this.clinicName,
    required this.walletAddress,
    required this.clinicId,
  });

  @override
  State<_ClinicHomePage> createState() => _ClinicHomePageState();
}

class _ClinicHomePageState extends State<_ClinicHomePage> {
  double _balance = 0.0;
  bool _isLoading = true;
  List<Map<String, dynamic>> _pendingRequests = [];
  final BlockchainService _blockchainService = BlockchainService();

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    await Future.wait([_loadBalance(), _loadPendingRequests()]);
  }

  Future<void> _loadBalance() async {
    if (widget.walletAddress.isNotEmpty) {
      final balance = await _blockchainService.getBalance(widget.walletAddress);
      if (mounted) {
        setState(() {
          _balance = balance;
          _isLoading = false;
        });
      }
    } else {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _loadPendingRequests() async {
    final requests = await PaymentService.getPendingClinicPayments(widget.clinicId);
    if (mounted) {
      setState(() {
        _pendingRequests = requests;
      });
    }
  }

  @override
  void dispose() {
    _blockchainService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.softBlueBackground,
      appBar: AppBar(
        backgroundColor: AppColors.softBlueBackground,
        elevation: 0,
        title: const Text(
          'Clinic Dashboard',
          style: TextStyle(color: AppColors.darkGrey),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: AppColors.darkGrey),
            onPressed: _loadData,
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const CircleAvatar(
                  radius: 22,
                  backgroundColor: Color(0xFF1976D2),
                  child: Icon(
                    Icons.local_hospital_outlined,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Welcome, ${widget.clinicName}',
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: Colors.black87,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: _ClinicStatCard(
                    title: 'Wallet Balance',
                    value: _isLoading 
                      ? 'Loading...' 
                      : BlockchainService.formatBalance(_balance),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _ClinicStatCard(
                    title: 'Pending Requests',
                    value: '${_pendingRequests.length}',
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            const Text(
              'Recent Requests',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w600,
                color: AppColors.darkGrey,
              ),
            ),
            const SizedBox(height: 8),
            if (_pendingRequests.isEmpty)
              const _EmptyStateCard(
                icon: Icons.receipt_long_outlined,
                message: 'No pending payment requests',
              )
            else
              ..._pendingRequests.take(5).map((request) => _PaymentRequestCard(
                patientName: request['patient']?['firstName'] ?? 'Patient',
                description: request['serviceDescription'] ?? '',
                amount: (request['amountDue'] ?? 0).toDouble(),
                status: request['status'] ?? 'UNPAID',
              )),
          ],
        ),
      ),
    );
  }
}

class _ClinicRequestPaymentPage extends StatefulWidget {
  final int clinicId;
  
  const _ClinicRequestPaymentPage({required this.clinicId});

  @override
  State<_ClinicRequestPaymentPage> createState() => _ClinicRequestPaymentPageState();
}

class _ClinicRequestPaymentPageState extends State<_ClinicRequestPaymentPage> {
  final _walletController = TextEditingController();
  final _amountController = TextEditingController();
  final _descriptionController = TextEditingController();
  bool _isLoading = false;

  Future<void> _sendPaymentRequest() async {
    if (_walletController.text.isEmpty || 
        _amountController.text.isEmpty || 
        _descriptionController.text.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please fill all fields')),
      );
      return;
    }

    setState(() => _isLoading = true);

    try {
      final result = await PaymentService.createPaymentRequest(
        clinicId: widget.clinicId,
        patientWallet: _walletController.text.trim(),
        amount: double.parse(_amountController.text),
        description: _descriptionController.text.trim(),
      );

      if (mounted) {
        if (result != null) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Payment request sent successfully!'),
              backgroundColor: Colors.green,
            ),
          );
          _walletController.clear();
          _amountController.clear();
          _descriptionController.clear();
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('Failed to send payment request. Check patient wallet address.'),
              backgroundColor: Colors.red,
            ),
          );
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  @override
  void dispose() {
    _walletController.dispose();
    _amountController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'New Payment Request',
          style: TextStyle(color: Colors.black87),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            TextField(
              controller: _walletController,
              decoration: const InputDecoration(
                labelText: 'Patient Wallet Address',
                border: OutlineInputBorder(),
                hintText: '0x...',
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _amountController,
              decoration: const InputDecoration(
                labelText: 'Amount (ETH)',
                border: OutlineInputBorder(),
              ),
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _descriptionController,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Description',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF1976D2),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
                onPressed: _isLoading ? null : _sendPaymentRequest,
                child: _isLoading
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Text('Send Payment Request'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ClinicHistoryPage extends StatefulWidget {
  final int clinicId;
  
  const _ClinicHistoryPage({required this.clinicId});

  @override
  State<_ClinicHistoryPage> createState() => _ClinicHistoryPageState();
}

class _ClinicHistoryPageState extends State<_ClinicHistoryPage> {
  List<Map<String, dynamic>> _allPayments = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadPayments();
  }

  Future<void> _loadPayments() async {
    final payments = await PaymentService.getClinicPayments(widget.clinicId);
    if (mounted) {
      setState(() {
        _allPayments = payments;
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Request History',
          style: TextStyle(color: Colors.black87),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Colors.black54),
            onPressed: () {
              setState(() => _isLoading = true);
              _loadPayments();
            },
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _allPayments.isEmpty
              ? const Center(
                  child: _EmptyStateCard(
                    icon: Icons.history,
                    message: 'No payment history yet',
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _allPayments.length,
                  itemBuilder: (context, index) {
                    final payment = _allPayments[index];
                    return _PaymentRequestCard(
                      patientName: payment['patient']?['firstName'] ?? 'Patient',
                      description: payment['serviceDescription'] ?? '',
                      amount: (payment['amountDue'] ?? 0).toDouble(),
                      status: payment['status'] ?? 'UNPAID',
                    );
                  },
                ),
    );
  }
}

class _ClinicSettingsPage extends StatelessWidget {
  const _ClinicSettingsPage();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Settings',
          style: TextStyle(color: Colors.black87),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Clinic profile, language, notifications, logout.',
              style: TextStyle(
                color: Colors.black54,
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 16),
            Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                children: [
                  ListTile(
                    leading: const Icon(Icons.apartment_outlined),
                    title: const Text('Clinic Profile'),
                    subtitle: const Text('Clinic information'),
                    onTap: () {},
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.notifications_outlined),
                    title: const Text('Notifications'),
                    subtitle: const Text('New requests, payment confirmations'),
                    onTap: () {},
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.support_agent_outlined),
                    title: const Text('Support / Contact'),
                    subtitle: const Text('Help, technical support, FAQ'),
                    onTap: () {},
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading:
                        const Icon(Icons.logout, color: Colors.redAccent),
                    title: const Text('Logout'),
                    onTap: () {
                      Navigator.of(context)
                          .popUntil((route) => route.isFirst);
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ClinicStatCard extends StatelessWidget {
  final String title;
  final String value;

  const _ClinicStatCard({
    required this.title,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.mint.withOpacity(0.12),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 12,
              color: Colors.black54,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: AppColors.darkGrey,
            ),
          ),
        ],
      ),
    );
  }
}

class _PaymentRequestCard extends StatelessWidget {
  final String patientName;
  final String description;
  final double amount;
  final String status;

  const _PaymentRequestCard({
    required this.patientName,
    required this.description,
    required this.amount,
    required this.status,
  });

  @override
  Widget build(BuildContext context) {
    final isPaid = status == 'PAID';
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: isPaid ? Colors.green[100] : Colors.orange[100],
          child: Icon(
            isPaid ? Icons.check : Icons.pending,
            color: isPaid ? Colors.green : Colors.orange,
          ),
        ),
        title: Text(patientName),
        subtitle: Text(description),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(
              '${amount.toStringAsFixed(4)} ETH',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
              decoration: BoxDecoration(
                color: isPaid ? Colors.green[50] : Colors.orange[50],
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                isPaid ? 'Paid' : 'Pending',
                style: TextStyle(
                  fontSize: 11,
                  color: isPaid ? Colors.green : Colors.orange,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _EmptyStateCard extends StatelessWidget {
  final IconData icon;
  final String message;

  const _EmptyStateCard({
    required this.icon,
    required this.message,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 64,
              color: Colors.grey[300],
            ),
            const SizedBox(height: 16),
            Text(
              message,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey[500],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
