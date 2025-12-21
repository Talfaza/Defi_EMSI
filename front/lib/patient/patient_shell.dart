import 'package:flutter/material.dart';
import '../app_colors.dart';
import '../services/blockchain_service.dart';
import '../services/payment_service.dart';

class PatientShell extends StatefulWidget {
  final String userName;
  final String walletAddress;
  final int userId;
  
  const PatientShell({
    super.key,
    required this.userName,
    required this.walletAddress,
    required this.userId,
  });

  @override
  State<PatientShell> createState() => _PatientShellState();
}

class _PatientShellState extends State<PatientShell> {
  int _currentIndex = 0;

  @override
  Widget build(BuildContext context) {
    final List<Widget> pages = [
      _PatientHomePage(
        userName: widget.userName,
        walletAddress: widget.walletAddress,
        userId: widget.userId,
      ),
      _PatientPaymentsPage(
        userId: widget.userId,
        walletAddress: widget.walletAddress,
      ),
      _PatientHistoryPage(userId: widget.userId),
      const _PatientSettingsPage(),
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
            icon: Icon(Icons.payment_outlined),
            label: 'Payments',
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

class _PatientHomePage extends StatefulWidget {
  final String userName;
  final String walletAddress;
  final int userId;
  
  const _PatientHomePage({
    required this.userName,
    required this.walletAddress,
    required this.userId,
  });

  @override
  State<_PatientHomePage> createState() => _PatientHomePageState();
}

class _PatientHomePageState extends State<_PatientHomePage> {
  double _balance = 0.0;
  double _totalToPay = 0.0;
  bool _isLoading = true;
  List<Map<String, dynamic>> _pendingPayments = [];
  final BlockchainService _blockchainService = BlockchainService();

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    await Future.wait([_loadBalance(), _loadPendingPayments()]);
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

  Future<void> _loadPendingPayments() async {
    final payments = await PaymentService.getPendingPatientPayments(widget.userId);
    if (mounted) {
      double total = 0.0;
      for (var payment in payments) {
        total += (payment['amountDue'] ?? 0).toDouble();
      }
      setState(() {
        _pendingPayments = payments;
        _totalToPay = total;
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
          'Home',
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
            Text(
              'Welcome, ${widget.userName}',
              style: const TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.bold,
                color: Colors.black87,
              ),
            ),
            const SizedBox(height: 4),
            const Text(
              'Manage your payments and track your requests in real time.',
              style: TextStyle(
                fontSize: 14,
                color: AppColors.lightGrey,
              ),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                _DashboardShortcutCard(
                  label: 'Payments',
                  color: AppColors.mint,
                  icon: Icons.payment_outlined,
                  badge: _pendingPayments.isNotEmpty ? '${_pendingPayments.length}' : null,
                  onTap: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (context) => _PatientPaymentsPage(
                          userId: widget.userId,
                          walletAddress: widget.walletAddress,
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(width: 12),
                _DashboardShortcutCard(
                  label: 'History',
                  color: AppColors.softYellow,
                  icon: Icons.history,
                  onTap: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (context) => _PatientHistoryPage(userId: widget.userId),
                      ),
                    );
                  },
                ),
                const SizedBox(width: 12),
                _DashboardShortcutCard(
                  label: 'Wallet',
                  color: AppColors.softPink,
                  icon: Icons.account_balance_wallet_outlined,
                  onTap: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (context) => WalletPage(
                          walletAddress: widget.walletAddress,
                        ),
                      ),
                    );
                  },
                ),
              ],
            ),
            const SizedBox(height: 24),
            Row(
              children: [
                Expanded(
                  child: _InfoCard(
                    title: 'Available Balance',
                    value: _isLoading 
                      ? 'Loading...' 
                      : BlockchainService.formatBalance(_balance),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _InfoCard(
                    title: 'To Pay',
                    value: '${_totalToPay.toStringAsFixed(4)} ETH',
                    highlight: _totalToPay > 0,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Pending Payments',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: AppColors.darkGrey,
                  ),
                ),
                if (_pendingPayments.isNotEmpty)
                  Text(
                    '${_pendingPayments.length} pending',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.orange[700],
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 8),
            if (_pendingPayments.isEmpty)
              const _EmptyStateCard(
                icon: Icons.receipt_long_outlined,
                message: 'No pending payments',
              )
            else
              ..._pendingPayments.take(3).map((payment) => _PaymentCard(
                clinicName: payment['clinic']?['name'] ?? 'Clinic',
                description: payment['serviceDescription'] ?? '',
                amount: (payment['amountDue'] ?? 0).toDouble(),
                status: payment['status'] ?? 'UNPAID',
              )),
          ],
        ),
      ),
    );
  }
}

class _PatientPaymentsPage extends StatefulWidget {
  final int userId;
  final String walletAddress;
  
  const _PatientPaymentsPage({
    required this.userId,
    required this.walletAddress,
  });

  @override
  State<_PatientPaymentsPage> createState() => _PatientPaymentsPageState();
}

class _PatientPaymentsPageState extends State<_PatientPaymentsPage> {
  List<Map<String, dynamic>> _pendingPayments = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadPayments();
  }

  Future<void> _loadPayments() async {
    final payments = await PaymentService.getPendingPatientPayments(widget.userId);
    if (mounted) {
      setState(() {
        _pendingPayments = payments;
        _isLoading = false;
      });
    }
  }

  Future<void> _payRequest(Map<String, dynamic> payment) async {
    final requestId = payment['requestId'] as String;
    final amount = (payment['amountDue'] ?? 0).toDouble();
    final clinicName = payment['clinic']?['name'] ?? 'Clinic';
    
    // Show dialog to enter private key
    final privateKeyController = TextEditingController();
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Confirm Payment'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Pay ${amount.toStringAsFixed(4)} ETH to $clinicName'),
            const SizedBox(height: 16),
            const Text(
              'Enter your private key to sign the transaction:',
              style: TextStyle(fontSize: 12, color: Colors.black54),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: privateKeyController,
              decoration: const InputDecoration(
                hintText: '0x... or paste private key',
                border: OutlineInputBorder(),
                isDense: true,
              ),
              style: const TextStyle(fontSize: 12, fontFamily: 'monospace'),
            ),
            const SizedBox(height: 8),
            const Text(
              '⚠️ Demo only - never share your private key in production!',
              style: TextStyle(fontSize: 10, color: Colors.orange),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF1976D2)),
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Pay Now'),
          ),
        ],
      ),
    );

    if (confirmed != true || privateKeyController.text.isEmpty) {
      privateKeyController.dispose();
      return;
    }

    final privateKey = privateKeyController.text.trim();
    privateKeyController.dispose();

    // Show loading
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Processing blockchain transaction...')),
    );

    // Execute blockchain payment via backend
    final result = await PaymentService.payWithPrivateKey(requestId, privateKey);
    
    if (mounted) {
      ScaffoldMessenger.of(context).hideCurrentSnackBar();
      
      if (result != null && result['success'] == true) {
        final txHash = result['transactionHash'] ?? '';
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Payment successful! TX: ${txHash.substring(0, 10)}...'),
            backgroundColor: Colors.green,
            duration: const Duration(seconds: 5),
          ),
        );
        _loadPayments();
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Payment failed: ${result?['error'] ?? 'Unknown error'}'),
            backgroundColor: Colors.red,
          ),
        );
      }
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
          'Payment Requests',
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
          : _pendingPayments.isEmpty
              ? const Center(
                  child: _EmptyStateCard(
                    icon: Icons.payment_outlined,
                    message: 'No pending payment requests',
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _pendingPayments.length,
                  itemBuilder: (context, index) {
                    final payment = _pendingPayments[index];
                    return _PayableCard(
                      clinicName: payment['clinic']?['name'] ?? 'Clinic',
                      description: payment['serviceDescription'] ?? '',
                      amount: (payment['amountDue'] ?? 0).toDouble(),
                      onPay: () => _payRequest(payment),
                    );
                  },
                ),
    );
  }
}

class _PatientHistoryPage extends StatefulWidget {
  final int userId;
  
  const _PatientHistoryPage({required this.userId});

  @override
  State<_PatientHistoryPage> createState() => _PatientHistoryPageState();
}

class _PatientHistoryPageState extends State<_PatientHistoryPage> {
  List<Map<String, dynamic>> _allPayments = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadPayments();
  }

  Future<void> _loadPayments() async {
    final payments = await PaymentService.getPatientPayments(widget.userId);
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
          'Payment History',
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
                    return _PaymentCard(
                      clinicName: payment['clinic']?['name'] ?? 'Clinic',
                      description: payment['serviceDescription'] ?? '',
                      amount: (payment['amountDue'] ?? 0).toDouble(),
                      status: payment['status'] ?? 'UNPAID',
                    );
                  },
                ),
    );
  }
}

class _PatientSettingsPage extends StatelessWidget {
  const _PatientSettingsPage();

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
              'Profile, security, language, logout.',
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
                    leading: const Icon(Icons.person_outline),
                    title: const Text('Profile'),
                    subtitle: const Text('Your account information'),
                    onTap: () {},
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.notifications_outlined),
                    title: const Text('Notifications'),
                    subtitle: const Text('Payment alerts and messages'),
                    onTap: () {},
                  ),
                  const Divider(height: 1),
                  ListTile(
                    leading: const Icon(Icons.support_agent_outlined),
                    title: const Text('Support / Contact'),
                    subtitle: const Text('Help, FAQ, technical support'),
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

class _DashboardShortcutCard extends StatelessWidget {
  final String label;
  final Color color;
  final IconData icon;
  final VoidCallback onTap;
  final String? badge;

  const _DashboardShortcutCard({
    required this.label,
    required this.color,
    required this.icon,
    required this.onTap,
    this.badge,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 10),
          decoration: BoxDecoration(
            color: color.withOpacity(0.3),
            borderRadius: BorderRadius.circular(18),
          ),
          child: Stack(
            children: [
              Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(
                    icon,
                    color: Colors.black87,
                    size: 22,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    label,
                    style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: Colors.black87,
                    ),
                  ),
                ],
              ),
              if (badge != null)
                Positioned(
                  right: 0,
                  top: 0,
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                      color: Colors.red,
                      shape: BoxShape.circle,
                    ),
                    child: Text(
                      badge!,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  final String title;
  final String value;
  final bool highlight;

  const _InfoCard({
    required this.title,
    required this.value,
    this.highlight = false,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: highlight 
            ? Colors.orange.withOpacity(0.12)
            : AppColors.mint.withOpacity(0.12),
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
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: highlight ? Colors.orange[700] : AppColors.darkGrey,
            ),
          ),
        ],
      ),
    );
  }
}

class _PaymentCard extends StatelessWidget {
  final String clinicName;
  final String description;
  final double amount;
  final String status;

  const _PaymentCard({
    required this.clinicName,
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
        title: Text(clinicName),
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

class _PayableCard extends StatelessWidget {
  final String clinicName;
  final String description;
  final double amount;
  final VoidCallback onPay;

  const _PayableCard({
    required this.clinicName,
    required this.description,
    required this.amount,
    required this.onPay,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  backgroundColor: Colors.blue[100],
                  child: const Icon(Icons.local_hospital, color: Colors.blue),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        clinicName,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                      Text(
                        description,
                        style: const TextStyle(
                          color: Colors.black54,
                          fontSize: 13,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Amount Due',
                      style: TextStyle(color: Colors.black54, fontSize: 12),
                    ),
                    Text(
                      '${amount.toStringAsFixed(4)} ETH',
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 20,
                        color: Color(0xFF1976D2),
                      ),
                    ),
                  ],
                ),
                ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF1976D2),
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                  ),
                  onPressed: onPay,
                  icon: const Icon(Icons.payment),
                  label: const Text('Pay Now'),
                ),
              ],
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

class WalletPage extends StatefulWidget {
  final String walletAddress;
  
  const WalletPage({super.key, required this.walletAddress});

  @override
  State<WalletPage> createState() => _WalletPageState();
}

class _WalletPageState extends State<WalletPage> {
  double _balance = 0.0;
  bool _isLoading = true;
  final BlockchainService _blockchainService = BlockchainService();

  @override
  void initState() {
    super.initState();
    _loadBalance();
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

  @override
  void dispose() {
    _blockchainService.dispose();
    super.dispose();
  }

  String _shortenAddress(String address) {
    if (address.length > 12) {
      return '${address.substring(0, 6)}...${address.substring(address.length - 4)}';
    }
    return address;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: const Text(
          'Wallet',
          style: TextStyle(color: Colors.black87),
        ),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFF1976D2), Color(0xFF42A5F5)],
                ),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Wallet Balance',
                    style: TextStyle(color: Colors.white70, fontSize: 14),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    _isLoading 
                      ? 'Loading...' 
                      : BlockchainService.formatBalance(_balance),
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 32,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    widget.walletAddress.isNotEmpty 
                      ? _shortenAddress(widget.walletAddress)
                      : 'Wallet not connected',
                    style: const TextStyle(color: Colors.white70, fontSize: 12),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            if (widget.walletAddress.isNotEmpty) ...[
              const Text(
                'Wallet Address',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: Colors.black54,
                ),
              ),
              const SizedBox(height: 8),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: Colors.grey[100],
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  widget.walletAddress,
                  style: const TextStyle(
                    fontSize: 12,
                    fontFamily: 'monospace',
                  ),
                ),
              ),
              const SizedBox(height: 24),
            ],
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF1976D2),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
                onPressed: _loadBalance,
                icon: const Icon(Icons.refresh),
                label: const Text('Refresh Balance'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
